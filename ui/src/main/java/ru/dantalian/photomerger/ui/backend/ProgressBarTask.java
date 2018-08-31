package ru.dantalian.photomerger.ui.backend;

import java.util.TimerTask;

import javax.swing.JProgressBar;

import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.ui.ProgressStateManager;

public class ProgressBarTask extends TimerTask implements ProgressStateManager {

	private final JProgressBar progressBar;
	private final TaskTrigger trigger;

	private volatile boolean started;

	private volatile String progressText = "";
	private volatile String progressCur = "";
	private volatile String progressMax = "";

	public ProgressBarTask(final TaskTrigger trigger, JProgressBar progressBar) {
		this.trigger = trigger;
		this.progressBar = progressBar;
	}

	@Override
	public void run() {
		if (started) {
			String str = "";
			if (!progressText.isEmpty()) {
				str += progressText;
			}
			if (!progressCur.isEmpty()) {
				str += ": " + progressCur;
			}
			if (!progressMax.isEmpty()) {
				str += " of " + progressMax;
			}
			progressBar.setString(str);
		}
	}

	@Override
	public void startProcess(DirItem targetDir) {
		this.trigger.startStop(true, targetDir);
	}

	@Override
	public void stopProcess() {
		this.trigger.startStop(false, null);
	}

	@Override
	public boolean isStarted() {
		return this.started;
	}

	public void setStarted(final boolean started) {
		this.started = started;
	}

	@Override
	public void setProgressText(String aText) {
		this.progressText = aText;
	}

	@Override
	public void setCurrent(String aCurrent, int aPercent) {
		this.progressCur = aCurrent;
		if (aPercent >= 0) {
			this.progressBar.setIndeterminate(false);
			this.progressBar.setValue(aPercent);
		} else {
			this.progressBar.setIndeterminate(true);
		}
	}

	@Override
	public void setMax(final String aMax) {
		this.progressMax = aMax;
	}

}
