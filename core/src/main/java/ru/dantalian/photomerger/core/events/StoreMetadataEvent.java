package ru.dantalian.photomerger.core.events;

import ru.dantalian.photomerger.core.model.TaskEvent;

public class StoreMetadataEvent extends TaskEvent<ProgressEventItem> {

	public static final String TOPIC = "store-metadata";

	public StoreMetadataEvent(final ProgressEventItem item) {
		this(TOPIC, item);
	}
	
	public StoreMetadataEvent(final long current, final long total) {
		this(TOPIC, current, total);
	}
	
	public StoreMetadataEvent(final String topic, final long current, final long total) {
		this(topic, new ProgressEventItem(current, total));
	}

	public StoreMetadataEvent(final String topic, final ProgressEventItem item) {
		super(topic, item);
	}

}
