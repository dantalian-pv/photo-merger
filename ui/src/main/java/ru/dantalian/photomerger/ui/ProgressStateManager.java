package ru.dantalian.photomerger.ui;

import ru.dantalian.photomerger.core.model.DirItem;

public interface ProgressStateManager {

	void startProcess(DirItem targetDir);

	void stopProcess();

	boolean isStarted();

	void setStarted(boolean started);

	void setProgressText(String aText);

	void setCurrent(String aCurrent, int aPercent);

	void setMax(String aMax);

}
