package ru.dantalian.photomerger.core.events;

import ru.dantalian.photomerger.core.events.MergeMetadataEvent.MergeMetadataItem;
import ru.dantalian.photomerger.core.model.TaskEvent;

public class MergeMetadataEvent extends TaskEvent<MergeMetadataItem> {

	public static final String TOPIC = "merge-metadata";

	public MergeMetadataEvent(final MergeMetadataItem item) {
		this(TOPIC, item);
	}

	public static MergeMetadataItem newItem(final long current, final long total) {
		return new MergeMetadataItem(current, total);
	}

	public MergeMetadataEvent(final String topic, final MergeMetadataItem item) {
		super(topic, item);
	}
	
	public static class MergeMetadataItem {

		private final long current;
		private final long total;

		public MergeMetadataItem(final long current, final long total) {
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
