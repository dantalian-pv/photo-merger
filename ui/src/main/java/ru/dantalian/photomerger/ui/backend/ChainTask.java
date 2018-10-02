package ru.dantalian.photomerger.ui.backend;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimerTask;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.core.ExecutionTask;
import ru.dantalian.photomerger.core.backend.ChainStoppedException;
import ru.dantalian.photomerger.core.backend.EventManagerFactory;
import ru.dantalian.photomerger.core.backend.tasks.CalculateFilesTask;
import ru.dantalian.photomerger.core.backend.tasks.MergeFilesTask;
import ru.dantalian.photomerger.core.backend.tasks.MergeMetadataTask;
import ru.dantalian.photomerger.core.backend.tasks.StoreMetadataTask;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;
import ru.dantalian.photomerger.ui.ProgressStateManager;
import ru.dantalian.photomerger.ui.elements.InterfaceStrings;

public final class ChainTask extends TimerTask {

	private static final Logger logger = LoggerFactory.getLogger(ChainTask.class);

	private final ProgressStateManager progress;

	private final DirItem targetDir;

	private final boolean copy;

	private final boolean keepPath;

	private final List<DirItem> scrDirs;

	private final ResourceBundle messages;
	
	private volatile ExecutionTask<?> currentTask;

	public ChainTask(final ProgressStateManager progress, final DirItem targetDir,
			List<DirItem> scrDirs, final boolean copy, final boolean keepPath,
			ResourceBundle messages) {
		this.progress = progress;
		this.targetDir = targetDir;
		this.scrDirs = Collections.unmodifiableList(scrDirs);
		this.copy = copy;
		this.keepPath = keepPath;
		this.messages = messages;
	}

	@Override
	public void run() {
		long filesCount = 0;
		Exception ex = null;
		long duplicates = 0;
		try {
			checkState();
			final EventManager events = EventManagerFactory.getInstance();
			final CalculateFilesTask calculateFilesTask = new CalculateFilesTask(
					scrDirs, targetDir, events);
			this.currentTask = calculateFilesTask;
			final List<Future<Long>> calculateFiles = calculateFilesTask.execute();
			long calcCount = 0;
			for (final Future<Long> future : calculateFiles) {
				checkState();
				calcCount += future.get();
			}
			checkState();
			filesCount = calcCount;
			calculateFilesTask.interrupt();

			checkState();
			final StoreMetadataTask storeMetadataTask = new StoreMetadataTask(scrDirs, targetDir, filesCount,
					events);
			this.currentTask = storeMetadataTask;
			final List<Future<List<DirItem>>> storeMetadata = storeMetadataTask.execute();
			final List<DirItem> metadataFiles = new LinkedList<>();
			for (final Future<List<DirItem>> future : storeMetadata) {
				checkState();
				metadataFiles.addAll(future.get());
			}
			checkState();
			storeMetadataTask.interrupt();

			checkState();
			// Merging all metadata files into one
			final MergeMetadataTask mergeTask = new MergeMetadataTask(targetDir, metadataFiles, events);
			this.currentTask = mergeTask;
			final DirItem metadataFile = mergeTask.execute().iterator().next().get();
			mergeTask.interrupt();

			checkState();
			final MergeFilesTask mergeFiles = new MergeFilesTask(targetDir,
					metadataFile,
					copy,
					keepPath,
					filesCount,
					events);
			this.currentTask = mergeFiles;
			duplicates = mergeFiles.execute().iterator().next().get();
			mergeFiles.interrupt();
		} catch (InterruptedException e) {
			logger.error("Failed to calculate files", e);
			ex = e;
		} catch (final ChainStoppedException e) {
			ex = e;
		} catch (final Exception e) {
			logger.error("Executing chain failed", e);
			ex = e;
		} finally {
			this.currentTask = null;
			if (ex == null) {
				this.progress.stopProcess(MessageFormat.format(messages.getString(InterfaceStrings.MERGED),
						filesCount, duplicates), 100);
				logger.info("Succesfully finished merging {} files. Found {} duplicates", filesCount, duplicates);
			} else if (ex instanceof ChainStoppedException) {
				this.progress.stopProcess(messages.getString(InterfaceStrings.ABORTED), 0);
			} else {
				this.progress.stopProcess(messages.getString(InterfaceStrings.ERROR), 0);
			}
		}
	}
	
	public void interrupt() {
		if (this.currentTask != null) {
			this.currentTask.interrupt();
		}
	}

	private void checkState() throws ChainStoppedException {
		if (!this.progress.isStarted()) {
			throw new ChainStoppedException();
		}
	}

}
