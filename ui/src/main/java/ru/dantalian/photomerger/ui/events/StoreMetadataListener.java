package ru.dantalian.photomerger.ui.events;

import java.util.ResourceBundle;

import ru.dantalian.photomerger.core.events.StoreMetadataEvent;
import ru.dantalian.photomerger.ui.ProgressCalculator;
import ru.dantalian.photomerger.ui.ProgressStateManager;
import ru.dantalian.photomerger.ui.elements.InterfaceStrings;

public class StoreMetadataListener extends AbstractProgressEventListener<StoreMetadataEvent> {

	public StoreMetadataListener(final ProgressStateManager progressManager, final ProgressCalculator calculator,
			final ResourceBundle messages) {
		super(progressManager, calculator, InterfaceStrings.STORING_METADATA, messages);
	}

}
