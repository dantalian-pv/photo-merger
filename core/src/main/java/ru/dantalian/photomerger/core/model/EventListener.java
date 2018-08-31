package ru.dantalian.photomerger.core.model;

public interface EventListener<T> {

	void handle(T event);

}
