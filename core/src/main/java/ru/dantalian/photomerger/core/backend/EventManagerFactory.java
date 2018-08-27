package ru.dantalian.photomerger.core.backend;

import ru.dantalian.photomerger.core.model.EventManager;

public class EventManagerFactory {

	private static final CoreEventManager INSTANCE = new CoreEventManager();

	public static final EventManager getInstance() {
		return INSTANCE;
	}

}
