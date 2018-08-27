package ru.dantalian.photomerger.core.events;

import ru.dantalian.photomerger.core.model.TaskEvent;

public class CalculateFilesEvent extends TaskEvent<Long> {

	public static final String TOPIC = "calculate-files";

	public CalculateFilesEvent(final Long item) {
		this(TOPIC, item);
	}

	public CalculateFilesEvent(final String topic, final Long item) {
		super(topic, item);
	}

}
