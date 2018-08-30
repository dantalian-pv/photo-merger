package ru.dantalian.photomerger.cli.events;

import me.tongfei.progressbar.ProgressBar;
import ru.dantalian.photomerger.core.events.MergeFilesEvent;

public class MergeMetadataListener extends AbstractProgressEventListener<MergeFilesEvent> {

	public MergeMetadataListener(final ProgressBar progressBar) {
		super(progressBar, "Merging metadata");
	}

}
