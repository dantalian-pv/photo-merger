package ru.dantalian.photomerger.cli.events;

import me.tongfei.progressbar.ProgressBar;
import ru.dantalian.photomerger.core.events.CalculateFilesEvent;
import ru.dantalian.photomerger.core.model.EventListener;

public class CalculateFilesListener implements EventListener<CalculateFilesEvent> {

	private final ProgressBar progressBar;

	public CalculateFilesListener(final ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	@Override
	public void handle(final CalculateFilesEvent item) {
		this.progressBar.setExtraMessage("Found files");
		this.progressBar.maxHint(-1L);
		this.progressBar.stepTo(item.getItem().longValue());
	}

}
