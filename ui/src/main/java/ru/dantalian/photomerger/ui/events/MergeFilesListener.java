package ru.dantalian.photomerger.ui.events;

import ru.dantalian.photomerger.core.events.MergeFilesEvent;
import ru.dantalian.photomerger.ui.ProgressCalculator;
import ru.dantalian.photomerger.ui.ProgressStateManager;

public class MergeFilesListener extends AbstractProgressEventListener<MergeFilesEvent> {

	public MergeFilesListener(ProgressStateManager progressManager, ProgressCalculator calculator) {
		super(progressManager, calculator, "Merging files");
	}

}
