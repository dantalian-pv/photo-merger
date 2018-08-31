package ru.dantalian.photomerger.ui.events;

import ru.dantalian.photomerger.core.model.TaskEvent;
import ru.dantalian.photomerger.ui.events.ProgressBarEvent.ProgressBarMessage;

public class ProgressBarEvent extends TaskEvent<ProgressBarMessage> {

	public static final String TOPIC = "progress-bar";

	public ProgressBarEvent(final ProgressBarMessage item) {
		super(TOPIC, item);
	}
	
	public ProgressBarEvent(final String message, final int value) {
		this(TOPIC, message, value);
	}
	
	public ProgressBarEvent(final String topic, final String message, final int value) {
		this(topic, new ProgressBarMessage(message, value));
	}

	public ProgressBarEvent(final String topic, final ProgressBarMessage item) {
		super(topic, item);
	}

	public static class ProgressBarMessage {

		private final String message;

		private final int value;

		public ProgressBarMessage(final String message, final int value) {
			this.message = message;
			this.value = value;
		}

		public String getMessage() {
			return this.message;
		}

		public int getValue() {
			return this.value;
		}

	}

}
