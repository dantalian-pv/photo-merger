package ru.dantalian.photomerger.ui;

public interface ProgressStateManager {

	void startProcess();

	boolean isStarted();

	void setProgress(String aText, int aPercent);

	void stopProcess(String text, int percent);

}
