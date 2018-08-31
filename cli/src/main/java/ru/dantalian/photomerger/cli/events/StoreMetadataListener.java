package ru.dantalian.photomerger.cli.events;

import me.tongfei.progressbar.ProgressBar;
import ru.dantalian.photomerger.core.events.MergeFilesEvent;

public class StoreMetadataListener extends AbstractProgressEventListener<MergeFilesEvent> {

	public StoreMetadataListener(final ProgressBar progressBar) {
		super(progressBar, "Storing metadata");
	}

}
