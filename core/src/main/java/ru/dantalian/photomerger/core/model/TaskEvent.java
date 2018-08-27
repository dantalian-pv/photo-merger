package ru.dantalian.photomerger.core.model;

public class TaskEvent<T> {

	private final String topic;

	private final T item;

	public TaskEvent(final String topic, final T item) {
		this.topic = topic;
		this.item = item;
	}

	public String getTopic() {
		return topic;
	}

	public T getItem() {
		return item;
	}

	@Override
	public String toString() {
		return "TaskEvent [topic=" + topic + ", item=" + item + "]";
	}

}
