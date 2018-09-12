package ru.dantalian.photomerger.core.backend;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import ru.dantalian.photomerger.core.AbstractExecutionTask;
import ru.dantalian.photomerger.core.TaskExecutionException;
import ru.dantalian.photomerger.core.backend.commands.StoreMetadataCommand;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;
import ru.dantalian.photomerger.core.utils.LocalFileTreeWalker;

public class StoreMetadataTask extends AbstractExecutionTask<List<DirItem>> {

	private final EventManager events;

	private final List<DirItem> sourceDirs;

	private final DirItem targetDir;

	private final long totalCount;

	private final AtomicLong filesCount = new AtomicLong(0);

	private final ThreadPoolExecutor pool;

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
