package ru.dantalian.photomerger.core.events;

import ru.dantalian.photomerger.core.model.TaskEvent;

public class MergeMetadataEvent extends TaskEvent<ProgressEventItem> {

	public static final String TOPIC = "merge-metadata";

	public MergeMetadataEvent(final ProgressEventItem item) {
		this(TOPIC, item);
	}
	
	public MergeMetadataEvent(final long current, final long total) {
		this(TOPIC, current, total);
	}
	
	public MergeMetadataEvent(final String topic, final long current, final long total) {
		this(topic, new ProgressEventItem(current, total));
	}

	public MergeMetadataEvent(final String topic, final ProgressEventItem item) {
		super(topic, item);
	}

}
