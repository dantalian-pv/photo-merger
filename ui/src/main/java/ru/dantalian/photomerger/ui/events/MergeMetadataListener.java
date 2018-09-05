package ru.dantalian.photomerger.ui.events;

import java.util.ResourceBundle;

import ru.dantalian.photomerger.core.events.MergeMetadataEvent;
import ru.dantalian.photomerger.ui.ProgressCalculator;
import ru.dantalian.photomerger.ui.ProgressStateManager;
import ru.dantalian.photomerger.ui.elements.InterfaceStrings;

public class MergeMetadataListener extends AbstractProgressEventListener<MergeMetadataEvent> {

	public MergeMetadataListener(final ProgressStateManager progressManager, final ProgressCalculator calculator,
			final ResourceBundle messages) {
		super(progressManager, calculator, InterfaceStrings.MERGING_METADATA, messages);
	}

}
