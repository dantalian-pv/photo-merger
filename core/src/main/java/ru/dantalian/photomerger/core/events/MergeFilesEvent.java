package ru.dantalian.photomerger.core.events;

import ru.dantalian.photomerger.core.model.TaskEvent;

public class MergeFilesEvent extends TaskEvent<ProgressEventItem> {

	public static final String TOPIC = "merge-files";

	public MergeFilesEvent(final ProgressEventItem item) {
		this(TOPIC, item);
	}

	public MergeFilesEvent(final long current, final long total) {
		this(TOPIC, current, total);
	}
	
	public MergeFilesEvent(final String topic, final long current, final long total) {
		this(topic, new ProgressEventItem(current, total));
	}

	public MergeFilesEvent(final String topic, final ProgressEventItem item) {
		super(topic, item);
	}

}
