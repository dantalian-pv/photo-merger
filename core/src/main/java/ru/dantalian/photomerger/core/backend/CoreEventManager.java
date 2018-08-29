package ru.dantalian.photomerger.core.backend;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.dantalian.photomerger.core.model.EventListener;
import ru.dantalian.photomerger.core.model.EventManager;
import ru.dantalian.photomerger.core.model.TaskEvent;

public class CoreEventManager implements EventManager {

	private final Map<String, List<EventListener<?>>> listeners;

	public CoreEventManager() {
		this.listeners = new HashMap<>();
	}
	
	@Override
	public <I> void publish(TaskEvent<I> event) {
		publish(event.getTopic(), event);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I> void publish(final String topic, final TaskEvent<I> event) {
		if (topic == null || event == null) {
			throw new NullPointerException();
		}
		final List<EventListener<?>> list = this.listeners.get(topic);
		if (list == null) {
			return;
		}
		for (final EventListener<?> listener: list) {
			((EventListener<TaskEvent<I>>) listener).handle(event);
		}
	}

	@Override
	public <I> void subscribe(final String topic, final EventListener<? extends TaskEvent<I>> listener) {
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
	public <I> void unsubscribe(String topic, Class<EventListener<I>> listenerClass) {
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
