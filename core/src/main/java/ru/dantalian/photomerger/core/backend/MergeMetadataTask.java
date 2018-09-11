package ru.dantalian.photomerger.core.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.core.AbstractExecutionTask;
import ru.dantalian.photomerger.core.TaskExecutionException;
import ru.dantalian.photomerger.core.events.MergeMetadataEvent;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;
import ru.dantalian.photomerger.core.model.FileItem;
import ru.dantalian.photomerger.core.utils.FileItemUtils;

public class MergeMetadataTask extends AbstractExecutionTask<DirItem> {

	private static final Logger logger = LoggerFactory.getLogger(MergeMetadataTask.class);

	private static final String METADATA_FILE_NAME = ".merged";

	private static final String METADATA_DIR_NAME = ".metadata";

	private final AtomicLong counter = new AtomicLong(0L);

	private final EventManager events;

	private final DirItem targetDir;

	private final long totalCount;

	private final AtomicLong filesCount = new AtomicLong(0);

	private final ThreadPoolExecutor pool;

	private final List<DirItem> metadataFiles;

	public MergeMetadataTask(final DirItem targetDir,
			final List<DirItem> metadataFiles, final EventManager events) {
		this(targetDir, metadataFiles, events, ThreadPoolFactory.getThreadPool(ThreadPoolFactory.MERGE_META_POOL));
	}

	public MergeMetadataTask(final DirItem targetDir, final List<DirItem> metadataFiles,
			final EventManager events, final ThreadPoolExecutor pool) {
		this.events = events;
		this.targetDir = targetDir;
		this.metadataFiles = Collections.unmodifiableList(metadataFiles);
		this.pool = pool;

		// Geometric progression with b1 = size q = 1/2 and n = log2(size)
		totalCount = (long) (metadataFiles.size() * 2 * (1 - Math.pow(0.5,
				(Math.log(metadataFiles.size()) / Math.log(2))))) + 1L;
	}

	@Override
	protected List<Future<DirItem>> execute0() throws TaskExecutionException {
		List<DirItem> metadataFiles = new LinkedList<>(this.metadataFiles);
		while (true) {
			if (metadataFiles.size() == 1) {
				final DirItem finalItem = metadataFiles.get(0);
				logger.info("Complete merged metadata {}", finalItem);
				return Collections.singletonList(CompletableFuture.completedFuture(finalItem));
			}
			final List<MergeCommand> commands = new LinkedList<>();

			final Iterator<DirItem> iterator = metadataFiles.iterator();
			boolean createEmpty = true;
			while (iterator.hasNext()) {
				if (this.interrupted.get()) {
					return Collections.singletonList(CompletableFuture.completedFuture(iterator.next()));
				}
				final DirItem left = iterator.next();
				this.events.publish(new MergeMetadataEvent(filesCount.incrementAndGet(), totalCount));
				if (!iterator.hasNext()) {
					metadataFiles = new LinkedList<>();
					metadataFiles.add(left);
					createEmpty = false;
					break;
				}
				final DirItem right = iterator.next();
				this.events.publish(new MergeMetadataEvent(filesCount.incrementAndGet(), totalCount));

				commands.add(new MergeCommand(left, right));
			}

			try {
				final List<Future<DirItem>> futures = pool.invokeAll(commands);
				if (createEmpty) {
					metadataFiles = new LinkedList<>();
				}
				for (final Future<DirItem> future : futures) {
					metadataFiles.add(future.get());
				}
			} catch (final InterruptedException | ExecutionException e) {
				throw new TaskExecutionException("Failed to merge metadata", e);
			}
		}
	}

	private Path getMetadataPath(final DirItem targetDir) {
		final File metadataDir = targetDir.getDir().toPath()
				.resolve(METADATA_DIR_NAME).toFile();
		if (!metadataDir.exists()) {
			metadataDir.mkdirs();
		}
		return metadataDir.toPath()
				.resolve(METADATA_FILE_NAME + "-" + counter.incrementAndGet());
	}

	class MergeCommand implements Callable<DirItem> {

		private final DirItem left;
		private final DirItem right;

		public MergeCommand(final DirItem left, final DirItem right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public DirItem call() throws Exception {
			final Path mergedPath = getMetadataPath(MergeMetadataTask.this.targetDir);
			try (final BufferedReader leftReader = new BufferedReader(new FileReader(left.getDir()));
					final BufferedReader rightReader = new BufferedReader(new FileReader(right.getDir()));
					final PrintWriter writer = new PrintWriter(new FileWriter(mergedPath.toFile()))) {
				final Iterator<String> leftIterator = leftReader.lines().iterator();
				final Iterator<String> rightIterator = rightReader.lines().iterator();
				FileItem leftItem = null;
				FileItem rightItem = null;
				while (leftIterator.hasNext() || rightIterator.hasNext()) {
					leftItem = (leftIterator.hasNext() && leftItem == null) ? FileItemUtils
							.createFileItem(leftIterator.next(), false) : leftItem;
					rightItem = (rightIterator.hasNext() && rightItem == null) ? FileItemUtils
							.createFileItem(rightIterator.next(), false) : rightItem;
					if (leftItem == null && rightItem != null) {
						writer.println(FileItemUtils.externalize(rightItem));
						rightItem = null;
					} else if (leftItem != null && rightItem == null) {
						writer.println(FileItemUtils.externalize(leftItem));
						leftItem = null;
					} else {
						if (leftItem != null && leftItem.compareTo(rightItem) == 0) {
							writer.println(FileItemUtils.externalize(leftItem));
							leftItem = null;
							writer.println(FileItemUtils.externalize(rightItem));
							rightItem = null;
						} else if (leftItem != null && leftItem.compareTo(rightItem) < 0) {
							writer.println(FileItemUtils.externalize(leftItem));
							leftItem = null;
						} else {
							writer.println(FileItemUtils.externalize(rightItem));
							rightItem = null;
						}
					}
				}
			} finally {
				left.getDir().delete();
				right.getDir().delete();
			}
			return new DirItem(mergedPath.toFile());
		}

	}

}
