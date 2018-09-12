package ru.dantalian.photomerger.core.backend;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DaemonThreadFactory implements ThreadFactory {
	
	private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

  private final AtomicInteger threadNumber = new AtomicInteger(1);
  
  private final String name;

	public DaemonThreadFactory(final String name) {
		this.name = name;
	}

	@Override
	public Thread newThread(final Runnable r) {
      final Thread thread = defaultFactory.newThread(r);
      if (!thread.isDaemon()) {
          thread.setDaemon(true);
      }
      thread.setName(name + "-" + threadNumber.getAndIncrement());
      return thread;
	}

}
