package ru.dantalian.photomerger.core.model;

public interface EventManager {

	<T> void publish(String topic, T item);

	<T> void subscribe(String topic, EventListener<T> listener);

	<T> void unsubscribe(String topic, Class<T> listenerClass);

}
