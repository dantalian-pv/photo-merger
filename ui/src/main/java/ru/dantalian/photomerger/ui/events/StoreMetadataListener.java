package ru.dantalian.photomerger.ui.events;

import ru.dantalian.photomerger.core.events.MergeFilesEvent;
import ru.dantalian.photomerger.ui.ProgressCalculator;
import ru.dantalian.photomerger.ui.ProgressStateManager;

public class StoreMetadataListener extends AbstractProgressEventListener<MergeFilesEvent> {

	public StoreMetadataListener(ProgressStateManager progressManager, ProgressCalculator calculator) {
		super(progressManager, calculator, "Storing metadata");
	}

}
