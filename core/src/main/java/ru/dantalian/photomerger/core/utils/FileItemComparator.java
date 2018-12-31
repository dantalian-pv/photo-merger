package ru.dantalian.photomerger.core.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.FileItem;

public class FileItemComparator implements Comparator<FileItem> {

	private final DirItem targetDir;

	public FileItemComparator(final DirItem aTargetDir) {
		targetDir = aTargetDir;
	}

	@Override
	public int compare(final FileItem aO1, final FileItem aO2) {
		if (aO1 == null && aO2 == null) {
			return 0;
		}
		if (aO1 == null) {
			return 1;
		}
		if (aO2 == null) {
			return -1;
		}
		// If one of the paths is target path, bring it to the top
		final boolean cp1 = targetDir.getPath().equals(aO1.getRootPath());
		final boolean cp2 = targetDir.getPath().equals(aO2.getRootPath());
		if (cp1 && !cp2) {
			return -1;
		}
		if (cp2 && !cp1) {
			return 1;
		}
		// On the other way compare relative paths depth
		final Path p1 = Paths.get(aO1.getPath().replace(aO1.getRootPath(), ""));
		final Path p2 = Paths.get(aO2.getPath().replace(aO2.getRootPath(), ""));
		return Integer.compare(p2.getNameCount(), p1.getNameCount());
	}

}
