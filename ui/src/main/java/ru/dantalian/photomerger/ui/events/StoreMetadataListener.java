package ru.dantalian.photomerger.ui.events;

import ru.dantalian.photomerger.core.events.StoreMetadataEvent;
import ru.dantalian.photomerger.core.model.EventListener;
import ru.dantalian.photomerger.ui.ProgressStateManager;

public class StoreMetadataListener implements EventListener<StoreMetadataEvent> {

	private final ProgressStateManager progressManager;

	public StoreMetadataListener(final ProgressStateManager progressManager) {
		this.progressManager = progressManager;
	}

	@Override
	public void handle(final StoreMetadataEvent event) {
		this.progressManager.setProgressText("Storing metadata");
		this.progressManager.setCurrent("" + event.getItem().getCurrent(),
				(int) (event.getItem().getCurrent() / event.getItem().getTotal() * 100));
		this.progressManager.setMax("" + event.getItem().getTotal());
	}

}
