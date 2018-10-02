package ru.dantalian.photomerger.core.backend.tasks;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import ru.dantalian.photomerger.core.AbstractExecutionTask;
import ru.dantalian.photomerger.core.TaskExecutionException;
import ru.dantalian.photomerger.core.backend.ThreadPoolFactory;
import ru.dantalian.photomerger.core.backend.commands.StoreMetadataCommand;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;
import ru.dantalian.photomerger.core.utils.LocalFileTreeWalker;
import ru.dantalian.photomerger.core.utils.Validator;

public class StoreMetadataTask extends AbstractExecutionTask<List<DirItem>> {

	private final EventManager events;

	private final List<DirItem> sourceDirs;

	private final DirItem targetDir;

	private final Long totalCount;

	private final AtomicLong filesCount = new AtomicLong(0);

	private final ThreadPoolExecutor pool;

	public StoreMetadataTask(final List<DirItem> sourceDirs, final DirItem targetDir,
			final Long totalCount, final EventManager events) {
		this(sourceDirs, targetDir, totalCount, events,
				ThreadPoolFactory.getThreadPool(ThreadPoolFactory.STORE_META_POOL));
	}

	public StoreMetadataTask(final List<DirItem> sourceDirs, final DirItem targetDir,
			final Long totalCount, final EventManager events, final ThreadPoolExecutor pool) {
		this.sourceDirs = Validator.checkEmptyCollection(sourceDirs);
		this.targetDir = Validator.checkNotNull(targetDir);
		this.totalCount = Validator.checkNotNull(totalCount);
		this.events = Validator.checkNotNull(events);
		this.pool = Validator.checkNotNull(pool);
	}

	@Override
	protected List<Future<List<DirItem>>> execute0() throws TaskExecutionException {
		final List<Future<List<DirItem>>> futures = new LinkedList<>();

		futures.add(pool.submit(new StoreMetadataCommand(targetDir, targetDir,
				new LocalFileTreeWalker(),
				this.events,
				this.filesCount,
				this.totalCount,
				this.interrupted)));
		for (final DirItem item: sourceDirs) {
			futures.add(pool.submit(new StoreMetadataCommand(item, targetDir,
					new LocalFileTreeWalker(),
					this.events,
					this.filesCount,
					this.totalCount,
					this.interrupted)));
		}
		return futures;
	}

}
