package ru.dantalian.photomerger.cli;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.tongfei.progressbar.ProgressBar;
import ru.dantalian.photomerger.cli.events.CalculateFilesListener;
import ru.dantalian.photomerger.cli.events.MergeFilesListener;
import ru.dantalian.photomerger.cli.events.MergeMetadataListener;
import ru.dantalian.photomerger.cli.events.StoreMetadataListener;
import ru.dantalian.photomerger.core.ExecutionTask;
import ru.dantalian.photomerger.core.TaskExecutionException;
import ru.dantalian.photomerger.core.backend.ChainStoppedException;
import ru.dantalian.photomerger.core.backend.EventManagerFactory;
import ru.dantalian.photomerger.core.backend.tasks.CalculateFilesTask;
import ru.dantalian.photomerger.core.backend.tasks.MergeFilesTask;
import ru.dantalian.photomerger.core.backend.tasks.MergeMetadataTask;
import ru.dantalian.photomerger.core.backend.tasks.StoreMetadataTask;
import ru.dantalian.photomerger.core.events.CalculateFilesEvent;
import ru.dantalian.photomerger.core.events.MergeFilesEvent;
import ru.dantalian.photomerger.core.events.MergeMetadataEvent;
import ru.dantalian.photomerger.core.events.StoreMetadataEvent;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;
import ru.dantalian.photomerger.core.utils.FileItemUtils;

public class TasksExecutor {

	private static final Logger logger = LoggerFactory.getLogger(TasksExecutor.class);

	private volatile boolean started;

	private volatile boolean interrupted;

	private volatile boolean finished;

	private final Queue<ExecutionTask<?>> tasks = new LinkedList<>();

	public TasksExecutor() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				started = false;
				ExecutionTask<?> task = null;
				synchronized (tasks) {
					while ((task = tasks.poll()) != null) {
						task.interrupt();
					}
				}
				while (!interrupted) {
					try {
						Thread.sleep(100);
					} catch (final InterruptedException e) {
						logger.warn("interrupted sleep", e);
					}
				}
				if (!finished) {
					System.err.println("Interrupting the process");
				}
			}
		}));
	}

	public void execute(final boolean copy, final boolean keep, final File target, final List<File> sources) {
		started = true;

		long filesCount = 0;
		Exception ex = null;
		long duplicates = 0;
		try {
			validate(target, sources);

			final List<DirItem> sourceDirs = sources.stream()
					.map(src -> new DirItem(src)).collect(Collectors.toList());
			final DirItem targetDir = new DirItem(target);

			checkState();
			try (final ProgressBar pb = new ProgressBar("Running...", filesCount, 1000)) {
				initProgressListeners(pb);

				final EventManager events = EventManagerFactory.getInstance();
				final CalculateFilesTask calculateFilesTask = new CalculateFilesTask(
						sourceDirs, targetDir, events);
				if (addTaskToQueue(calculateFilesTask)) {
					return;
				}
				final List<Future<Long>> calculateFiles = calculateFilesTask.execute();
				long calcCount = 0;
				for (final Future<Long> future: calculateFiles) {
					checkState();
					calcCount += future.get();
				}
				checkState();
				filesCount = calcCount;
				calculateFilesTask.interrupt();

				final StoreMetadataTask storeMetadataTask = new StoreMetadataTask(sourceDirs, targetDir, filesCount,
						events);
				if (addTaskToQueue(storeMetadataTask)) {
					return;
				}
				final List<Future<List<DirItem>>> storeMetadata = storeMetadataTask.execute();
				final List<DirItem> metadataFiles = new LinkedList<>();
				for (final Future<List<DirItem>> future: storeMetadata) {
					checkState();
					metadataFiles.addAll(future.get());
				}
				checkState();
				storeMetadataTask.interrupt();

				// Merging all metadata files into one
				final MergeMetadataTask mergeTask = new MergeMetadataTask(targetDir, metadataFiles, events);
				if (addTaskToQueue(mergeTask)) {
					return;
				}
				final DirItem metadataFile = mergeTask.execute().iterator().next().get();
				mergeTask.interrupt();

				final MergeFilesTask mergeFiles = new MergeFilesTask(targetDir,
						metadataFile,
						copy,
						keep,
						filesCount,
						events);
				duplicates = mergeFiles.execute().iterator().next().get();
				mergeFiles.interrupt();
			}
		} catch (final InterruptedException e) {
			logger.error("Failed to calculate files", e);
			System.err.println("An error has occured. See log for further details");
			ex = e;
		} catch(final ChainStoppedException e) {
			// Ignore it
			ex = e;
		} catch(final Exception e) {
			System.err.println("An error has occured. See log for further details");
			logger.error("Executing chain failed", e);
			ex = e;
		} finally {
			started = false;
			interrupted = true;
			if (ex == null) {
				finished = true;
				System.out.println("Merged " + filesCount + " files. Found " + duplicates + " duplicates");
			} else if (ex instanceof ChainStoppedException) {
				System.out.println("The process was interrupted");
				interrupted = true;
			} else {
				logger.error("Error occured during the merge process", ex);
			}
		}
	}

	private boolean addTaskToQueue(final ExecutionTask<?> calculateFilesTask) throws ChainStoppedException {
		synchronized (tasks) {
			if (!started) {
				return true;
			}
			tasks.add(calculateFilesTask);
		}
		checkState();
		return false;
	}

	private void validate(final File target, final List<File> sources) throws TaskExecutionException {
		if (!target.exists()) {
			throw new TaskExecutionException("target '" + target + "' does not exist");
		}
		if (!target.isDirectory()) {
			throw new TaskExecutionException("target '" + target + "' is not directory");
		}
		for (final File src: sources) {
			if (!src.exists()) {
				throw new TaskExecutionException("source '" + src + "' does not exist");
			}
			if (!src.isDirectory()) {
				throw new TaskExecutionException("source '" + src + "' is not directory");
			}
			if (FileItemUtils.hasParentInSource(target.toPath(), src.toPath())) {
				throw new TaskExecutionException("Target dir should be different than source dirs");
			}
		}
	}

	private void initProgressListeners(final ProgressBar progressBar) {
		final EventManager events = EventManagerFactory.getInstance();
		events.subscribe(CalculateFilesEvent.TOPIC, new CalculateFilesListener(progressBar));
		events.subscribe(StoreMetadataEvent.TOPIC, new StoreMetadataListener(progressBar));
		events.subscribe(MergeMetadataEvent.TOPIC, new MergeMetadataListener(progressBar));
		events.subscribe(MergeFilesEvent.TOPIC, new MergeFilesListener(progressBar));
	}

	private void checkState() throws ChainStoppedException {
		if (!started) {
			throw new ChainStoppedException();
		}
	}

}
