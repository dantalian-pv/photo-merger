package ru.dantalian.photomerger.core.events;

import ru.dantalian.photomerger.core.events.StoreMetadataEvent.StoreMetadataItem;
import ru.dantalian.photomerger.core.model.TaskEvent;

public class StoreMetadataEvent extends TaskEvent<StoreMetadataItem> {

	public static final String TOPIC = "store-metadata";

	public StoreMetadataEvent(final StoreMetadataItem item) {
		this(TOPIC, item);
	}

	public static StoreMetadataItem newItem(final long current, final long total) {
		return new StoreMetadataItem(current, total);
	}

	public StoreMetadataEvent(final String topic, final StoreMetadataItem item) {
		super(topic, item);
	}
	
	public static class StoreMetadataItem {

		private final long current;
		private final long total;

		public StoreMetadataItem(final long current, final long total) {
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

}
