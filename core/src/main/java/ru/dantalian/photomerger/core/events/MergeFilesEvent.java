package ru.dantalian.photomerger.core.events;

import ru.dantalian.photomerger.core.events.MergeFilesEvent.MergeFilesItem;
import ru.dantalian.photomerger.core.model.TaskEvent;

public class MergeFilesEvent extends TaskEvent<MergeFilesItem> {

	public static final String TOPIC = "merge-files";

	public MergeFilesEvent(final MergeFilesItem item) {
		this(TOPIC, item);
	}

	public static MergeFilesItem newItem(final long current, final long total) {
		return new MergeFilesItem(current, total);
	}

	public MergeFilesEvent(final String topic, final MergeFilesItem item) {
		super(topic, item);
	}
	
	public static class MergeFilesItem {

		private final long current;
		private final long total;

		public MergeFilesItem(final long current, final long total) {
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
