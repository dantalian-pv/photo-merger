package ru.dantalian.photomerger.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.ProgressStateManager;
import ru.dantalian.photomerger.model.DirItem;
import ru.dantalian.photomerger.model.FileItem;
import ru.dantalian.photomerger.utils.FileItemUtils;

public class MergeMetadataTask {

	private static final Logger logger = LoggerFactory.getLogger(MergeMetadataTask.class);

	private static final String METADATA_FILE_NAME = ".merged";

	private static final String METADATA_DIR_NAME = ".metadata";

	private final AtomicLong counter = new AtomicLong(0L);

	private final ProgressStateManager progress;
	
	private final DirItem targetDir;

	private volatile long totalCount;

	private final AtomicLong filesCount = new AtomicLong(0);
	
	private final ThreadPoolExecutor pool;

	private final Timer timer = new Timer("merge-metadata-progress", true);

	public MergeMetadataTask(final ProgressStateManager progress, final DirItem targetDir) {
		this.progress = progress;
		this.targetDir = targetDir;
		this.pool = ThreadPoolFactory.getThreadPool(ThreadPoolFactory.MERGE_META_POOL);
	}

	public DirItem mergeMetadata(final List<DirItem> aMetadataFiles)
			throws InterruptedException, ExecutionException {
		filesCount.set(0);
		// Geometric progression with b1 = size q = 1/2 and n = log2(size)
		totalCount = (long) (aMetadataFiles.size() * 2 * (1 - Math.pow(0.5,
				(Math.log(aMetadataFiles.size())/Math.log(2))))) + 1L;

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (progress.isStarted() && filesCount.get() > 0L) {
					progress.setProgressText("Merging metadata");
					final int percent = 33 + (int) (filesCount.get() * 33L / totalCount);
					progress.setCurrent("" + filesCount.get(), percent);
					progress.setMax("" + totalCount);
				}
			}
		}, 1000, 1000);

		List<DirItem> metadataFiles = aMetadataFiles;
		while (true) {
			if (metadataFiles.size() == 1) {
				final DirItem finalItem = metadataFiles.get(0);
				logger.info("Complete merged metadata {}", finalItem);
				return finalItem;
			}
			final List<MergeCommand> commands = new LinkedList<>();

			final Iterator<DirItem> iterator = metadataFiles.iterator();
			boolean createEmpty = true;
			while (iterator.hasNext()) {
				final DirItem left = iterator.next();
				filesCount.incrementAndGet();
				if (!iterator.hasNext()) {
					metadataFiles = new LinkedList<>();
					metadataFiles.add(left);
					createEmpty = false;
					break;
				}
				final DirItem right = iterator.next();
				filesCount.incrementAndGet();
				
				commands.add(new MergeCommand(left, right));
			}
			
			final List<Future<DirItem>> futures = pool.invokeAll(commands);
			if (createEmpty) {
				metadataFiles = new LinkedList<>();
			}
			for (final Future<DirItem> future: futures) {
				metadataFiles.add(future.get());
			}
		}
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
			try(BufferedReader leftReader = new BufferedReader(new FileReader(left.getDir()));
					BufferedReader rightReader = new BufferedReader(new FileReader(right.getDir()));
					PrintWriter writer = new PrintWriter(new FileWriter(mergedPath.toFile()))) {
				final Iterator<String> leftIterator = leftReader.lines().iterator();
				final Iterator<String> rightIterator = rightReader.lines().iterator();
				FileItem leftItem = null;
				FileItem rightItem = null;
				while(leftIterator.hasNext() || rightIterator.hasNext()) {
					leftItem = (leftIterator.hasNext() && leftItem == null)
							? FileItemUtils.createFileItem(leftIterator.next(), false) : leftItem;
					rightItem = (rightIterator.hasNext() && rightItem == null)
							? FileItemUtils.createFileItem(rightIterator.next(), false) : rightItem;
					if (leftItem == null && rightItem != null) {
						writer.println(FileItemUtils.externalize(rightItem));
						rightItem = null;
					} else if (leftItem != null && rightItem == null) {
						writer.println(FileItemUtils.externalize(leftItem));
						leftItem = null;
					} else {
						if (leftItem.compareTo(rightItem) == 0) {
							writer.println(FileItemUtils.externalize(leftItem));
							leftItem = null;
							writer.println(FileItemUtils.externalize(rightItem));
							rightItem = null;
						} else if (leftItem.compareTo(rightItem) < 0) {
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
