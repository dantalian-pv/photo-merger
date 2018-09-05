package ru.dantalian.photomerger.ui.events;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import ru.dantalian.photomerger.core.events.CalculateFilesEvent;
import ru.dantalian.photomerger.core.model.EventListener;
import ru.dantalian.photomerger.ui.ProgressStateManager;
import ru.dantalian.photomerger.ui.elements.InterfaceStrings;

public class CalculateFilesListener implements EventListener<CalculateFilesEvent> {

	private final ProgressStateManager progressManager;

	private final ResourceBundle messages;

	public CalculateFilesListener(final ProgressStateManager progressManager, final ResourceBundle messages) {
		this.progressManager = progressManager;
		this.messages = messages;
	}

	@Override
	public void handle(final CalculateFilesEvent item) {
		this.progressManager.setProgress(
				MessageFormat.format(messages.getString(InterfaceStrings.FOUND_FILES), item.getItem()), -1);
	}

}
