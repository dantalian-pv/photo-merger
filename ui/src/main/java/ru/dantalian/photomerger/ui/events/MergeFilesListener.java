package ru.dantalian.photomerger.ui.events;

import ru.dantalian.photomerger.core.events.MergeFilesEvent;
import ru.dantalian.photomerger.core.model.EventListener;
import ru.dantalian.photomerger.ui.ProgressStateManager;

public class MergeFilesListener implements EventListener<MergeFilesEvent> {

	private final ProgressStateManager progressManager;

	public MergeFilesListener(final ProgressStateManager progressManager) {
		this.progressManager = progressManager;
	}

	@Override
	public void handle(final MergeFilesEvent event) {
		this.progressManager.setProgressText("Merging files");
		this.progressManager.setCurrent("for " + event.getItem().getCurrent(),
				(int) (event.getItem().getCurrent() / event.getItem().getTotal() * 100));
		this.progressManager.setMax("" + event.getItem().getTotal());
	}

}
