package ru.dantalian.photomerger;

public interface ProgressStateManager {

	public void startProcess();

	public void stopProcess();

	public boolean isStarted();

}
