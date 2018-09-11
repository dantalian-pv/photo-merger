package ru.dantalian.photomerger.core.backend.tasks;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.core.backend.FileTreeWalker;
import ru.dantalian.photomerger.core.backend.OnlyFileVisitor;
import ru.dantalian.photomerger.core.backend.VisitSingleFileCommand;
import ru.dantalian.photomerger.core.events.CalculateFilesEvent;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;

public class CalculateFilesSubtask implements Callable<Long> {
	
	private static final Logger logger = LoggerFactory.getLogger(CalculateFilesSubtask.class);

	private final DirItem dirItem;

	private final EventManager events;
	private FileTreeWalker fileTreeWalker;
	private final AtomicLong filesCount;
	private final AtomicBoolean interrupted;

	public CalculateFilesSubtask(final DirItem dirItem, final EventManager events,
			final FileTreeWalker fileTreeWalker,
			final AtomicLong filesCount, final AtomicBoolean interrupted) {
		this.dirItem = dirItem;
		this.events = events;
		this.fileTreeWalker = fileTreeWalker;
		this.filesCount = filesCount;
		this.interrupted = interrupted;
	}

	@Override
	public Long call() throws Exception {
		if (interrupted.get()) {
			logger.warn("Task interrupted");
			return Long.valueOf(0L);
		}
		logger.info("Calculating files in {}", dirItem);
		final IncrementCounterVisitor visitor = new IncrementCounterVisitor();
		fileTreeWalker.walkFileTree(this.dirItem.getDir().toPath(),
				Collections.singleton(FileVisitOption.FOLLOW_LINKS),
				Integer.MAX_VALUE,
				new OnlyFileVisitor(interrupted, visitor));
		logger.info("Finished calculating files in {}", dirItem);
		return visitor.getCount();
	}

	private class IncrementCounterVisitor implements VisitSingleFileCommand {
		
		private long count = 0;

		@Override
		public void visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			this.count++;
			events.publish(new CalculateFilesEvent(filesCount.incrementAndGet()));
		}

		public long getCount() {
			return this.count;
		}

	}

}
