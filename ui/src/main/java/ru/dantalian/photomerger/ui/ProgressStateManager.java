package ru.dantalian.photomerger.ui;

import ru.dantalian.photomerger.core.model.DirItem;

public interface ProgressStateManager {

	public void startProcess(DirItem targetDir);

	public void stopProcess();

	public boolean isStarted();

	public void setProgressText(String aText);

	public void setCurrent(String aCurrent, int aPercent);

	public void setMax(String aMax);

}
