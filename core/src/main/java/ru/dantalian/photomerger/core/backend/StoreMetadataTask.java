package ru.dantalian.photomerger.core.backend;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.core.AbstractExecutionTask;
import ru.dantalian.photomerger.core.TaskExecutionException;
import ru.dantalian.photomerger.core.events.StoreMetadataEvent;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;
import ru.dantalian.photomerger.core.model.FileItem;
import ru.dantalian.photomerger.core.utils.FileItemUtils;

public class StoreMetadataTask extends AbstractExecutionTask<List<DirItem>> {

	private static final Logger logger = LoggerFactory.getLogger(StoreMetadataTask.class);

	private static final String METADATA_FILE_NAME = ".metadata";

	private static final String METADATA_DIR_NAME = ".metadata";

	private static final int LIMIT = 1000;

	private final EventManager events;

	private final List<DirItem> sourceDirs;
	private final DirItem targetDir;

	private final long totalCount;

	private final AtomicLong filesCount = new AtomicLong(0);

	private final ThreadPoolExecutor pool;

	private final AtomicLong counter = new AtomicLong(0L);
	
	public StoreMetadataTask(final List<DirItem> sourceDirs, final DirItem targetDir,
			final long totalCount, final EventManager events) {
		this(sourceDirs, targetDir, totalCount, events,
				ThreadPoolFactory.getThreadPool(ThreadPoolFactory.STORE_META_POOL));
	}

	public StoreMetadataTask(final List<DirItem> sourceDirs, final DirItem targetDir,
			final long totalCount, final EventManager events, final ThreadPoolExecutor pool) {
		this.sourceDirs = sourceDirs;
		this.targetDir = targetDir;
		this.totalCount = totalCount;
		this.events = events;
		this.pool = pool;
	}

	public List<Future<List<DirItem>>> execute0() throws TaskExecutionException {
		final List<Future<List<DirItem>>> futures = new LinkedList<>();

		futures.add(pool.submit(new StoreMetadataCommand(targetDir, targetDir, this.interrupted)));
		for (final DirItem item: sourceDirs) {
			futures.add(pool.submit(new StoreMetadataCommand(item, targetDir, this.interrupted)));
		}

		return futures;
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

	class StoreMetadataCommand implements Callable<List<DirItem>> {

		private final DirItem sourceDir;

		private final DirItem targetDir;

		private AtomicBoolean interrupted;

		public StoreMetadataCommand(final DirItem sourceDir, final DirItem targetDir,
				final AtomicBoolean interrupted) {
			this.sourceDir = sourceDir;
			this.targetDir = targetDir;
			this.interrupted = interrupted;
		}

		@Override
		public List<DirItem> call() throws Exception {
			final List<DirItem> metadataFiles = new LinkedList<>();
			try (final WriteMetadataCommand command = new WriteMetadataCommand(sourceDir, targetDir,
					metadataFiles)) {
				Files.walkFileTree(this.sourceDir.getDir().toPath(),
						Collections.singleton(FileVisitOption.FOLLOW_LINKS),
						Integer.MAX_VALUE,
						new OnlyFileVisitor(this.interrupted, command));
			}
			return metadataFiles;
		}

	}

	class WriteMetadataCommand implements VisitSingleFileCommand, AutoCloseable {

		private final DirItem sourceDir;
		private final DirItem targetDir;
		private final List<DirItem> metadataFiles;

		private final List<FileItem> queue = new ArrayList<>(LIMIT);

		public WriteMetadataCommand(final DirItem sourceDir, final DirItem targetDir,
				final List<DirItem> metadataFiles) {
			this.sourceDir = sourceDir;
			this.targetDir = targetDir;
			this.metadataFiles = metadataFiles;
		}

		@Override
		public void visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
			if (queue.size() >= LIMIT) {
				writeData();
			}
			final FileItem fileItem = FileItemUtils.createFileItem(sourceDir.getDir(), file.toFile(), false);
			queue.add(fileItem);
			events.publish(StoreMetadataEvent.TOPIC, new StoreMetadataEvent(
					StoreMetadataEvent.newItem(filesCount.incrementAndGet(), totalCount)));
		}

		@Override
		public void close() throws Exception {
			if (!queue.isEmpty()) {
				writeData();
			}
		}

		private void writeData() throws IOException {
			final Path metadataPath = getMetadataPath(targetDir);
			logger.info("Storing metadata for {} to {} files = {}", sourceDir, metadataPath, queue.size());
			Collections.sort(queue);
			try (final PrintWriter writer = new PrintWriter(new BufferedOutputStream(
					new FileOutputStream(metadataPath.toFile())))) {
				for (final FileItem fileItem: queue) {
					writer.println(FileItemUtils.externalize(fileItem));
				}
			} finally {
				queue.clear();
				metadataFiles.add(new DirItem(metadataPath.toFile()));
			}
		}

	}

}
