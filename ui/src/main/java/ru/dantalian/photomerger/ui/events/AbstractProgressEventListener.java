package ru.dantalian.photomerger.ui.events;

import ru.dantalian.photomerger.core.events.ProgressEventItem;
import ru.dantalian.photomerger.core.model.EventListener;
import ru.dantalian.photomerger.core.model.TaskEvent;
import ru.dantalian.photomerger.ui.ProgressCalculator;
import ru.dantalian.photomerger.ui.ProgressStateManager;

public abstract class AbstractProgressEventListener<T extends TaskEvent<ProgressEventItem>> implements EventListener<T> {
	
	protected final ProgressStateManager progressManager;

	protected final ProgressCalculator calculator;

	protected final String progressText;

	public AbstractProgressEventListener(final ProgressStateManager progressManager,
			final ProgressCalculator calculator,
			final String progressText) {
		this.progressManager = progressManager;
		this.calculator = calculator;
		this.progressText = progressText;
	}
	
	@Override
	public void handle(final T event) {
		final ProgressEventItem item = event.getItem();
		this.progressManager.setProgressText(this.progressText);
		this.progressManager.setCurrent("for " + item.getCurrent(),
				this.calculator.calculate(item.getCurrent(), item.getTotal()));
		this.progressManager.setMax("" + item.getTotal());
	}

}
