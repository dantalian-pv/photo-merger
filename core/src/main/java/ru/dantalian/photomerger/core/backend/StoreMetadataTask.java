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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.core.ProgressStateManager;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.FileItem;
import ru.dantalian.photomerger.core.utils.FileItemUtils;

public class StoreMetadataTask {

	private static final Logger logger = LoggerFactory.getLogger(StoreMetadataTask.class);

	private static final String METADATA_FILE_NAME = ".metadata";

	private static final String METADATA_DIR_NAME = ".metadata";

	private static final int LIMIT = 1000;

	private final AtomicLong counter = new AtomicLong(0L);

	private final ProgressStateManager progress;

	private final long totalCount;

	private final AtomicLong filesCount = new AtomicLong(0);

	private final ThreadPoolExecutor pool;

	private final Timer timer = new Timer("store-metadata-progress", true);

	public StoreMetadataTask(final ProgressStateManager progress, final long totalCount) {
		this.progress = progress;
		this.totalCount = totalCount;
		this.pool = ThreadPoolFactory.getThreadPool(ThreadPoolFactory.STORE_META_POOL);
	}

	public List<Future<List<DirItem>>> storeMetadata(final List<DirItem> sourceDirs, final DirItem targetDir)
			throws InterruptedException {
		filesCount.set(0);

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (progress.isStarted() && filesCount.get() > 0L) {
					progress.setProgressText("Storing metadata");
					final int percent = (int) (filesCount.get() * 33L / totalCount);
					progress.setCurrent("" + filesCount.get(), percent);
					progress.setMax("" + totalCount);
				}
			}
		}, 1000, 1000);

		final List<Future<List<DirItem>>> futures = new LinkedList<>();

		futures.add(pool.submit(new StoreMetadataCommand(targetDir, targetDir)));
		for (final DirItem item: sourceDirs) {
			futures.add(pool.submit(new StoreMetadataCommand(item, targetDir)));
		}

		return futures;
	}

	public void finish() {
		timer.cancel();
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

		public StoreMetadataCommand(final DirItem sourceDir, final DirItem targetDir) {
			this.sourceDir = sourceDir;
			this.targetDir = targetDir;
		}

		@Override
		public List<DirItem> call() throws Exception {
			final List<DirItem> metadataFiles = new LinkedList<>();
			try (final WriteMetadataCommand command = new WriteMetadataCommand(sourceDir, targetDir,
					metadataFiles)) {
				Files.walkFileTree(this.sourceDir.getDir().toPath(),
						Collections.singleton(FileVisitOption.FOLLOW_LINKS),
						Integer.MAX_VALUE,
						new OnlyFileVisitor(progress, command));
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
			filesCount.incrementAndGet();
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
