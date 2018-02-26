package ru.dantalian.photomerger.backend;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.ProgressStateManager;
import ru.dantalian.photomerger.model.DirItem;

public class CalculateFilesTask {

	private static final Logger logger = LoggerFactory.getLogger(CalculateFilesTask.class);
	
	private final ProgressStateManager progress;
	
	private final AtomicLong filesCount = new AtomicLong(0);
	
	private final ThreadPoolExecutor pool = new ThreadPoolExecutor(4, 16, 1, TimeUnit.MINUTES,
			new LinkedBlockingQueue<>(), new DaemonThreadFactory("calculate-files"));
	
	private final Timer timer = new Timer("calculate-files-progress", true);

	public CalculateFilesTask(final ProgressStateManager progress) {
		this.progress = progress;
	}

	public List<Future<Boolean>> calculateFiles(final List<DirItem> sourceDirs, final DirItem targetDir) throws InterruptedException {
		filesCount.set(0);

		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				if (progress.isStarted() && filesCount.get() > 0L) {
					progress.setProgressText("Found " + filesCount.get() +" files to analyse");
				}
			}
		}, 1000, 1000);

		final List<Future<Boolean>> futures = new LinkedList<>();

		futures.add(pool.submit(new CalculateSubtask(targetDir), Boolean.TRUE));
		for (final DirItem item: sourceDirs) {
			futures.add(pool.submit(new CalculateSubtask(item), Boolean.TRUE));
		}

		return futures;
	}

	public void finish() {
		timer.cancel();
	}
	
	public long getFilesCount() {
		return filesCount.get();
	}

	class CalculateSubtask implements Runnable {

		private final DirItem dirItem;

		public CalculateSubtask(DirItem dirItem) {
			this.dirItem = dirItem;
		}

		@Override
		public void run() {
			try {
				logger.info("Calculating files in {}", dirItem);
				Files.walkFileTree(this.dirItem.getDir().toPath(),
						Collections.singleton(FileVisitOption.FOLLOW_LINKS),
						Integer.MAX_VALUE,
						new OnlyFileVisitor(progress, new IncrementCounterVisitor()));
				logger.info("Finished calculating files in {}", dirItem);
			} catch (IOException e) {
				logger.error("Failed to visit tree " + dirItem, e);
			}
		}
		
	}
	
	class IncrementCounterVisitor implements VisitSingleFileCommand {

		@Override
		public void visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			filesCount.incrementAndGet();
		}
		
	}

}
