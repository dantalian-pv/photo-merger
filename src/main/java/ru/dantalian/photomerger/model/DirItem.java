package ru.dantalian.photomerger.model;

import java.io.File;

public class DirItem {

	private final File dir;

	public DirItem(final File dir) {
		this.dir = dir;
	}
	
	public File getDir() {
		return this.dir;
	}
	
	public String getPath() {
		return this.dir.getPath();
	}
	
	public String getName() {
		return this.dir.getName();
	}

	@Override
	public String toString() {
		return dir.getPath();
	}
	
}
