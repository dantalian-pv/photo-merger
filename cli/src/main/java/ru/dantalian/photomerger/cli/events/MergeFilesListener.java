package ru.dantalian.photomerger.cli.events;

import me.tongfei.progressbar.ProgressBar;
import ru.dantalian.photomerger.core.events.MergeFilesEvent;

public class MergeFilesListener extends AbstractProgressEventListener<MergeFilesEvent> {

	public MergeFilesListener(final ProgressBar progressBar) {
		super(progressBar, "Merging files");
	}

}
