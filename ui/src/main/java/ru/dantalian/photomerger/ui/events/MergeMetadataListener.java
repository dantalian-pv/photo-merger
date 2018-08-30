package ru.dantalian.photomerger.ui.events;

import ru.dantalian.photomerger.core.events.MergeFilesEvent;
import ru.dantalian.photomerger.ui.ProgressCalculator;
import ru.dantalian.photomerger.ui.ProgressStateManager;

public class MergeMetadataListener extends AbstractProgressEventListener<MergeFilesEvent> {

	public MergeMetadataListener(ProgressStateManager progressManager, ProgressCalculator calculator) {
		super(progressManager, calculator, "Merging metadata");
	}

}
