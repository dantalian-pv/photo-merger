package ru.dantalian.photomerger.ui.backend;

import ru.dantalian.photomerger.ui.ProgressCalculator;

public class OffsetProgressCalculator implements ProgressCalculator {

	private final int offset;

	public OffsetProgressCalculator(final int offset) {
		this.offset = offset;
	}

	@Override
	public int calculate(long current, long total) {
		return (int) (current / total * 33 + offset);
	}

}
