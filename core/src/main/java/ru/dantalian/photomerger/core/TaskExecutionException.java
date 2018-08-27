package ru.dantalian.photomerger.core;

public class TaskExecutionException extends Exception {

	private static final long serialVersionUID = 5219158768563465797L;

	public TaskExecutionException() {
		super();
	}

	public TaskExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public TaskExecutionException(String message) {
		super(message);
	}

}
