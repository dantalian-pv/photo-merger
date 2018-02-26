package ru.dantalian.photomerger.backend;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.ProgressStateManager;
import ru.dantalian.photomerger.model.DirItem;

public class MergeMetadataTask {

	private static final Logger logger = LoggerFactory.getLogger(MergeMetadataTask.class);

	private static final String METADATA_FILE_NAME = ".merged";

	private static final String METADATA_DIR_NAME = ".metadata";

	private static final int LIMIT = 1000;

	private final AtomicLong counter = new AtomicLong(0L);

	private final ProgressStateManager progress;

	private final long totalCount;

	private final AtomicLong filesCount = new AtomicLong(0);
	
	private final ThreadPoolExecutor pool = new ThreadPoolExecutor(4, 16, 1, TimeUnit.MINUTES,
			new LinkedBlockingQueue<>(), new DaemonThreadFactory("store-metadata"));

	private final Timer timer = new Timer("store-metadata-progress", true);

	public MergeMetadataTask(final ProgressStateManager progress, final long totalCount) {
		this.progress = progress;
		this.totalCount = totalCount;
	}

	public DirItem mergeMetadata(final List<DirItem> aMetadataFiles)
			throws InterruptedException, ExecutionException {
		filesCount.set(0);

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (progress.isStarted() && filesCount.get() > 0L) {
					progress.setProgressText("Storing metadata");
					final int percent = (int) (filesCount.get() * 50L / totalCount);
					progress.setCurrent("" + filesCount.get(), percent);
					progress.setMax("" + totalCount);
				}
			}
		}, 1000, 1000);

		List<DirItem> metadataFiles = aMetadataFiles;
		while (true) {
			final List<MergeCommand> commands = new LinkedList<>();
			
			Iterator<DirItem> iterator = metadataFiles.iterator();
			while (iterator.hasNext()) {
				final DirItem left = iterator.next();
				if (!iterator.hasNext()) {
					return left;
				}
				final DirItem right = iterator.next();
				
				commands.add(new MergeCommand(left, right));
			}
			
			final List<Future<DirItem>> futures = pool.invokeAll(commands);
			metadataFiles = new LinkedList<>();
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
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
