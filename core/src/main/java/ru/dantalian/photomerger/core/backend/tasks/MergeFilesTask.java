package ru.dantalian.photomerger.core.backend.tasks;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.dantalian.photomerger.core.AbstractExecutionTask;
import ru.dantalian.photomerger.core.TaskExecutionException;
import ru.dantalian.photomerger.core.backend.ThreadPoolFactory;
import ru.dantalian.photomerger.core.backend.commands.MergeFilesCommand;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;

public class MergeFilesTask extends AbstractExecutionTask<Long> {

	private final DirItem targetDir;

	private final DirItem metadataFile;
	
	private final boolean copy;

	private final boolean keepPath;

	private final long totalCount;

	private final EventManager events;
	
	private final ThreadPoolExecutor pool;
	
	public MergeFilesTask(final DirItem targetDir,
			final DirItem metadataFile, boolean copy, boolean keepPath, long totalCount,
			final EventManager events) {
		this(targetDir, metadataFile, copy, keepPath, totalCount, events,
				ThreadPoolFactory.getThreadPool(ThreadPoolFactory.MERGE_FILES_POOL,
						1, 1, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>()));
	}

	public MergeFilesTask(final DirItem targetDir,
			final DirItem metadataFile, boolean copy, boolean keepPath, long totalCount,
			final EventManager events, final ThreadPoolExecutor pool) {
		this.targetDir = targetDir;
		this.metadataFile = metadataFile;
		this.copy = copy;
		this.keepPath = keepPath;
		this.totalCount = totalCount;
		this.events = events;
		this.pool = pool;
	}

	protected List<Future<Long>> execute0() throws TaskExecutionException {
		final List<Future<Long>> futures = new LinkedList<>();
		final Future<Long> future = this.pool.submit(new MergeFilesCommand(targetDir,
				metadataFile,
				copy,
				keepPath,
				totalCount,
				events,
				interrupted));
		futures.add(future);
		return futures;
	}

}
