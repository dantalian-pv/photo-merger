package ru.dantalian.photomerger.ui.backend;

import java.util.TimerTask;

import ru.dantalian.photomerger.core.model.EventManager;
import ru.dantalian.photomerger.ui.ProgressStateManager;
import ru.dantalian.photomerger.ui.events.ProgressBarEvent;

public class ProgressBarTask extends TimerTask implements ProgressStateManager {

	private final EventManager events;

	private volatile boolean started;

	private volatile String progressText;
	private volatile int percent;

	public ProgressBarTask(final EventManager events) {
		this.events = events;
	}

	@Override
	public void run() {
		if (this.started) {
			final String str = progressText != null && !progressText.isEmpty() ? progressText : "";
			this.events.publish(new ProgressBarEvent(str, percent));
		}
	}

	@Override
	public void startProcess() {
		this.started = true;
	}

	@Override
	public void stopProcess(final String text, final int percent) {
		this.started = false;
		this.events.publish(new ProgressBarEvent(text, percent));
	}

	@Override
	public boolean isStarted() {
		return this.started;
	}

	@Override
	public void setProgress(final String text, final int percent) {
		this.progressText = text;
		this.percent = percent;
	}

}
