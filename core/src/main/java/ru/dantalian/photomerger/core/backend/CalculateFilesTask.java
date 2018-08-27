package ru.dantalian.photomerger.core.backend;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.core.AbstractExecutionTask;
import ru.dantalian.photomerger.core.TaskExecutionException;
import ru.dantalian.photomerger.core.events.CalculateFilesEvent;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;

public class CalculateFilesTask extends AbstractExecutionTask<Long> {

	private static final Logger logger = LoggerFactory.getLogger(CalculateFilesTask.class);

	private final EventManager events;

	private final List<DirItem> sourceDirs;
	private final DirItem targetDir;

	private final AtomicLong filesCount = new AtomicLong(0);

	private final ThreadPoolExecutor pool;
	
	public CalculateFilesTask(final List<DirItem> sourceDirs, final DirItem targetDir,
			final EventManager events) {
		this(sourceDirs, targetDir, events, ThreadPoolFactory.getThreadPool(ThreadPoolFactory.CALC_FILES_POOL));
	}

	public CalculateFilesTask(final List<DirItem> sourceDirs, final DirItem targetDir,
			final EventManager events, final ThreadPoolExecutor pool) {
		this.sourceDirs = sourceDirs;
		this.targetDir = targetDir;
		this.events = events;
		this.pool = pool;
	}

	@Override
	public List<Future<Long>> execute0() throws TaskExecutionException {
		final List<Future<Long>> futures = new LinkedList<>();

		futures.add(pool.submit(new CalculateSubtask(targetDir)));
		for (final DirItem item : sourceDirs) {
			futures.add(pool.submit(new CalculateSubtask(item)));
		}
		return futures;
	}

	private class CalculateSubtask implements Callable<Long> {

		private final DirItem dirItem;

		public CalculateSubtask(final DirItem dirItem) {
			this.dirItem = dirItem;
		}

		@Override
		public Long call() throws Exception {
			logger.info("Calculating files in {}", dirItem);
			final IncrementCounterVisitor visitor = new IncrementCounterVisitor();
			Files.walkFileTree(this.dirItem.getDir().toPath(),
					Collections.singleton(FileVisitOption.FOLLOW_LINKS),
					Integer.MAX_VALUE,
					new OnlyFileVisitor(interrupted, visitor));
			logger.info("Finished calculating files in {}", dirItem);
			return visitor.getCount();
		}

	}

	private class IncrementCounterVisitor implements VisitSingleFileCommand {
	
		private long count = 0;

		@Override
		public void visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			this.count++;
			events.publish(CalculateFilesEvent.TOPIC, new CalculateFilesEvent(filesCount.incrementAndGet()));
		}

		public long getCount() {
			return this.count;
		}

	}

}
