package ru.dantalian.photomerger.ui.events;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import ru.dantalian.photomerger.core.events.ProgressEventItem;
import ru.dantalian.photomerger.core.model.EventListener;
import ru.dantalian.photomerger.core.model.TaskEvent;
import ru.dantalian.photomerger.ui.ProgressCalculator;
import ru.dantalian.photomerger.ui.ProgressStateManager;

public abstract class AbstractProgressEventListener<T extends TaskEvent<ProgressEventItem>> implements EventListener<T> {

	protected final ProgressStateManager progressManager;

	protected final ProgressCalculator calculator;

	protected final String progressText;

	protected final ResourceBundle messages;

	public AbstractProgressEventListener(final ProgressStateManager progressManager,
			final ProgressCalculator calculator,
			final String progressText,
			final ResourceBundle messages) {
		this.progressManager = progressManager;
		this.calculator = calculator;
		this.progressText = progressText;
		this.messages = messages;
	}

	@Override
	public void handle(final T event) {
		final ProgressEventItem item = event.getItem();
		this.progressManager.setProgress(MessageFormat.format(
				this.messages.getString(this.progressText),
				item.getCurrent(),
				Math.max(item.getCurrent(), item.getTotal())),
				this.calculator.calculate(item.getCurrent(), item.getTotal()));
	}

}
