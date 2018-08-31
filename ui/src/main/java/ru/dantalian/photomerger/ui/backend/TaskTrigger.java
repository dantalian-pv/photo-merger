package ru.dantalian.photomerger.ui.backend;

import ru.dantalian.photomerger.core.model.DirItem;

public interface TaskTrigger {

	void startStop(boolean start, DirItem targetDir);

}
