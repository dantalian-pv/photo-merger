package ru.dantalian.photomerger.backend;

public class ChainStoppedException extends Exception {

	private static final long serialVersionUID = -9202336022899198112L;

	public ChainStoppedException() {
		super();
	}

	public ChainStoppedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChainStoppedException(String message) {
		super(message);
	}

	public ChainStoppedException(Throwable cause) {
		super(cause);
	}
	
}
