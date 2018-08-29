package ru.dantalian.photomerger.core.events;

public class ProgressEventItem {

	private final long current;
	private final long total;

	public ProgressEventItem(final long current, final long total) {
		this.current = current;
		this.total = total;
	}

	public long getCurrent() {
		return current;
	}

	public long getTotal() {
		return total;
	}

}
