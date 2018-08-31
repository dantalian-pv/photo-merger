package ru.dantalian.photomerger.ui.events;

import ru.dantalian.photomerger.core.events.CalculateFilesEvent;
import ru.dantalian.photomerger.core.model.EventListener;
import ru.dantalian.photomerger.ui.ProgressStateManager;

public class CalculateFilesListener implements EventListener<CalculateFilesEvent> {

	private final ProgressStateManager progressManager;

	public CalculateFilesListener(final ProgressStateManager progressManager) {
		this.progressManager = progressManager;
	}

	@Override
	public void handle(final CalculateFilesEvent item) {
		this.progressManager.setProgress("Found files: " + item.getItem().toString(), -1);
	}

}
