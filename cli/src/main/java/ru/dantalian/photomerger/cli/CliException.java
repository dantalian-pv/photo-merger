package ru.dantalian.photomerger.cli;

public class CliException extends Exception {

	private static final long serialVersionUID = 1L;

	public CliException() {
		super();
	}

	public CliException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CliException(final String message) {
		super(message);
	}
	
}
