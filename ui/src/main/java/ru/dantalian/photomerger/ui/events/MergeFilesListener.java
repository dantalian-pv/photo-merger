package ru.dantalian.photomerger.ui.events;

import java.util.ResourceBundle;

import ru.dantalian.photomerger.core.events.MergeFilesEvent;
import ru.dantalian.photomerger.ui.ProgressCalculator;
import ru.dantalian.photomerger.ui.ProgressStateManager;
import ru.dantalian.photomerger.ui.elements.InterfaceStrings;

public class MergeFilesListener extends AbstractProgressEventListener<MergeFilesEvent> {

	public MergeFilesListener(final ProgressStateManager progressManager, final ProgressCalculator calculator,
			final ResourceBundle messages) {
		super(progressManager, calculator, InterfaceStrings.MERGING_FILES, messages);
	}

}
