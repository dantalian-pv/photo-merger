package ru.dantalian.photomerger.core;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractExecutionTask<T> implements ExecutionTask<T> {

	protected final AtomicBoolean interrupted;

	public AbstractExecutionTask() {
		this.interrupted = new AtomicBoolean(false);
	}

	@Override
	public void interrupt() {
		this.interrupted.set(true);
	}
	
	@Override
	public List<Future<T>> execute() throws TaskExecutionException {
		if (this.interrupted.get()) {
			throw new IllegalStateException("The task has been interrupted");
		}
		return this.execute0();
	}
	
	public abstract List<Future<T>> execute0() throws TaskExecutionException;

}
