package ru.dantalian.photomerger.core.backend.tasks;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import ru.dantalian.photomerger.core.AbstractExecutionTask;
import ru.dantalian.photomerger.core.TaskExecutionException;
import ru.dantalian.photomerger.core.backend.ThreadPoolFactory;
import ru.dantalian.photomerger.core.backend.commands.CalculateFilesCommand;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;
import ru.dantalian.photomerger.core.utils.LocalFileTreeWalker;
import ru.dantalian.photomerger.core.utils.Validator;

public class CalculateFilesTask extends AbstractExecutionTask<Long> {

	private final List<DirItem> sourceDirs;

	private final DirItem targetDir;

	private final EventManager events;

	private final ThreadPoolExecutor pool;

	private final AtomicLong filesCount = new AtomicLong(0);

	public CalculateFilesTask(final List<DirItem> sourceDirs, final DirItem targetDir,
			final EventManager events) {
		this(sourceDirs, targetDir, events, ThreadPoolFactory.getThreadPool(ThreadPoolFactory.CALC_FILES_POOL));
	}

	public CalculateFilesTask(final List<DirItem> sourceDirs, final DirItem targetDir,
			final EventManager events, final ThreadPoolExecutor pool) {
		this.sourceDirs = Validator.checkEmptyCollection(sourceDirs);
		this.targetDir = Validator.checkNotNull(targetDir);
		this.events = Validator.checkNotNull(events);
		this.pool = Validator.checkNotNull(pool);
	}

	@Override
	protected List<Future<Long>> execute0() throws TaskExecutionException {
		final List<Future<Long>> futures = new LinkedList<>();

		futures.add(pool.submit(new CalculateFilesCommand(
				this.targetDir,
				this.events,
				new LocalFileTreeWalker(),
				this.filesCount,
				this.interrupted)));
		for (final DirItem item : sourceDirs) {
			futures.add(pool.submit(new CalculateFilesCommand(
					item,
					this.events,
					new LocalFileTreeWalker(),
					this.filesCount,
					this.interrupted)));
		}
		return futures;
	}

}
