package ru.dantalian.photomerger.utils;

import java.io.IOException;

import ru.dantalian.photomerger.ao.FileItem;

public class FileItemUtils {
	
	public static final String SEPARATOR = ":";
	
	public static String externalize(final FileItem aFileItem) throws IOException {
		return "" + aFileItem.getChecksum()
			+ SEPARATOR
			+ aFileItem.getRootPath().getPath()
			+ SEPARATOR
			+ aFileItem.getFile().getPath();
	}

}
