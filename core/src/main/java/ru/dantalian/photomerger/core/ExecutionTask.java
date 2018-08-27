package ru.dantalian.photomerger.core;

import java.util.List;
import java.util.concurrent.Future;

public interface ExecutionTask<T> {

	public List<Future<T>> execute() throws TaskExecutionException;
	
	public void interrupt();

}
