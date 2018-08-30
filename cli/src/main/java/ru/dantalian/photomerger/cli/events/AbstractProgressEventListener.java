package ru.dantalian.photomerger.cli.events;

import me.tongfei.progressbar.ProgressBar;
import ru.dantalian.photomerger.core.events.ProgressEventItem;
import ru.dantalian.photomerger.core.model.EventListener;
import ru.dantalian.photomerger.core.model.TaskEvent;

public abstract class AbstractProgressEventListener<T extends TaskEvent<ProgressEventItem>> implements EventListener<T> {
	
	protected final ProgressBar progressBar;

	protected final String progressText;

	public AbstractProgressEventListener(final ProgressBar progressBar,
			final String progressText) {
		this.progressBar = progressBar;
		this.progressText = progressText;
	}
	
	@Override
	public void handle(final T event) {
		final ProgressEventItem item = event.getItem();
		this.progressBar.setExtraMessage(this.progressText);
		this.progressBar.maxHint(item.getTotal());
		this.progressBar.stepTo(item.getCurrent());
	}

}
