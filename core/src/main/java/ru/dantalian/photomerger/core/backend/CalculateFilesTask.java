package ru.dantalian.photomerger.core.backend;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.core.AbstractExecutionTask;
import ru.dantalian.photomerger.core.TaskExecutionException;
import ru.dantalian.photomerger.core.events.CalculateFilesEvent;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;

public class CalculateFilesTask extends AbstractExecutionTask<Boolean> {

	private static final Logger logger = LoggerFactory.getLogger(CalculateFilesTask.class);

	private final EventManager events;

	private final List<DirItem> sourceDirs;
	private final DirItem targetDir;

	private final AtomicLong filesCount = new AtomicLong(0);

	private final ThreadPoolExecutor pool;
	
	public CalculateFilesTask(final EventManager events,
			final List<DirItem> sourceDirs, final DirItem targetDir) {
		this(events, sourceDirs, targetDir, ThreadPoolFactory.getThreadPool(ThreadPoolFactory.CALC_FILES_POOL));
	}

	public CalculateFilesTask(final EventManager events,
			final List<DirItem> sourceDirs, final DirItem targetDir,
			final ThreadPoolExecutor pool) {
		this.events = events;
		this.sourceDirs = sourceDirs;
		this.targetDir = targetDir;

		this.pool = pool;
	}

	@Override
	public List<Future<Boolean>> execute0() throws TaskExecutionException {
		final List<Future<Boolean>> futures = new LinkedList<>();

		futures.add(pool.submit(new CalculateSubtask(targetDir, this.events, this.interrupted), Boolean.TRUE));
		for (final DirItem item : sourceDirs) {
			futures.add(pool.submit(new CalculateSubtask(item, this.events, this.interrupted), Boolean.TRUE));
		}

		this.interrupted.set(true);
		return futures;
	}

	private class CalculateSubtask implements Runnable {

		private final DirItem dirItem;

		private AtomicBoolean interrupted;

		private EventManager events;

		public CalculateSubtask(final DirItem dirItem,
				final EventManager events,
				final AtomicBoolean interrupted) {
			this.dirItem = dirItem;
			this.events = events;
			this.interrupted = interrupted;
		}

		@Override
		public void run() {
			try {
				logger.info("Calculating files in {}", dirItem);
				Files.walkFileTree(this.dirItem.getDir().toPath(),
						Collections.singleton(FileVisitOption.FOLLOW_LINKS),
						Integer.MAX_VALUE,
						new OnlyFileVisitor(this.interrupted, new IncrementCounterVisitor(this.events)));
				logger.info("Finished calculating files in {}", dirItem);
			} catch (IOException e) {
				logger.error("Failed to visit tree " + dirItem, e);
			}
		}

	}

	private class IncrementCounterVisitor implements VisitSingleFileCommand {

		private EventManager events;

		public IncrementCounterVisitor(EventManager events) {
			this.events = events;
		}

		@Override
		public void visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			this.events.publish(CalculateFilesEvent.TOPIC, new CalculateFilesEvent(filesCount.incrementAndGet()));
		}

	}

}
