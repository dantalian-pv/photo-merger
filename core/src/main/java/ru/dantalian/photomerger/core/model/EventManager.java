package ru.dantalian.photomerger.core.model;

public interface EventManager {
	
	<I> void publish(TaskEvent<I> event);

	<I> void publish(String topic, TaskEvent<I> event);

	<I> void subscribe(String topic, EventListener<? extends TaskEvent<I>> listener);

	<I> void unsubscribe(String topic, Class<EventListener<I>> listenerClass);

}
