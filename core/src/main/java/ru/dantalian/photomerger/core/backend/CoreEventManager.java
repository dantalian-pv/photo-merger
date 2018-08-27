package ru.dantalian.photomerger.core.backend;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.dantalian.photomerger.core.model.EventListener;
import ru.dantalian.photomerger.core.model.EventManager;

public class CoreEventManager implements EventManager {

	private final Map<String, List<EventListener<?>>> listeners;

	public CoreEventManager() {
		this.listeners = new HashMap<>();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void publish(final String topic, final T item) {
		if (topic == null || item == null) {
			throw new NullPointerException();
		}
		final List<EventListener<?>> list = this.listeners.get(topic);
		if (list == null) {
			return;
		}
		for (final EventListener<?> listener: list) {
			((EventListener<T>) listener).handle(item);
		}
	}

	@Override
	public <T> void subscribe(final String topic, final EventListener<T> listener) {
		if (topic == null || listener == null) {
			throw new NullPointerException();
		}
		synchronized (listeners) {
			List<EventListener<?>> list = this.listeners.get(topic);
			if (list == null) {
				list = new LinkedList<>();
				this.listeners.put(topic, list);
			}
			list.add(listener);
		}
	}
	
	@Override
	public <T> void unsubscribe(String topic, Class<T> listenerClass) {
		synchronized (listeners) {
			List<EventListener<?>> list = this.listeners.get(topic);
			if (list == null) {
				return;
			}
			final Iterator<EventListener<?>> iterator = list.iterator();
			while (iterator.hasNext()) {
				if(iterator.next().getClass().equals(listenerClass)) {
					iterator.remove();
				}
			}
		}
	}

}
