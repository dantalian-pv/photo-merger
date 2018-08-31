package ru.dantalian.photomerger.ui.backend;

import ru.dantalian.photomerger.ui.ProgressCalculator;

public class OffsetProgressCalculator implements ProgressCalculator {

	private final int offset;

	public OffsetProgressCalculator(final int offset) {
		this.offset = offset;
	}

	@Override
	public int calculate(final long current, final long total) {
		return (int) (1.0d * current / (1.0d * total) * 33.0 + offset);
	}

}
