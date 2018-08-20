package ru.dantalian.photomerger.backend;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ThreadPoolFactory {

	public static final String CALC_FILES_POOL = "calculate-files";

	public static final String STORE_META_POOL = "store-metadata";

	public static final String MERGE_META_POOL = "merge-metadata";

	private static final Map<String, ThreadPoolExecutor> POOL_MAP = new HashMap<>();

	private ThreadPoolFactory() {
	}

	public static ThreadPoolExecutor getThreadPool(final String name) {
		return getThreadPool(name, 4, 16, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
	}

	public static ThreadPoolExecutor getThreadPool(final String name,
			final int corePoolSize,
			final int maximumPoolSize,
			final long keepAliveTime,
			final TimeUnit unit,
			final BlockingQueue<Runnable> workQueue) {
		ThreadPoolExecutor pool = POOL_MAP.get(name);
		if (pool != null) {
			return pool;
		}
		synchronized (POOL_MAP) {
			pool = POOL_MAP.get(name);
			if (pool != null) {
				return pool;
			}
			pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
					workQueue, new DaemonThreadFactory(name));
			POOL_MAP.put(name, pool);
			return pool;
		}
	}

}
