package ru.dantalian.photomerger.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import ru.dantalian.photomerger.core.model.FileItem;

public class FileItemUtils {

	public static final String SEPARATOR = "::";

	private FileItemUtils() {
	}

	public static String externalize(final FileItem aFileItem) throws IOException {
		return aFileItem.getSize()
			+ SEPARATOR
			+ aFileItem.getRootPath()
			+ SEPARATOR
			+ aFileItem.getPath();
	}

	public static FileItem createFileItem(final File rootDir, final File file,
			final boolean aCalcCRC) throws IOException {
		validate(rootDir, file);
		final long crc = (aCalcCRC) ? calculateChecksum(file) : 0;
		return new FileItem(rootDir.getPath(), file.getPath(), crc, file.length());
	}

	public static FileItem createFileItem(final String crcAndPath, final boolean calcCrc)
			throws IOException {
		final String[] split = crcAndPath.split(SEPARATOR);
		if (split.length != 3) {
			throw new IllegalArgumentException("Wrong FileItem format " + crcAndPath);
		}
		final long size = Long.parseLong(split[0]);
		final File rootPath = new File(split[1]);
		final File file = new File(split[2]);
		validate(rootPath, file);
		final long crc = (calcCrc) ? calculateChecksum(file) : 0;
		return new FileItem(rootPath.getPath(), file.getPath(), crc, size);
	}

	public static long calculateChecksum(final File aFile) throws IOException {
		final byte[] buf = new byte[4096];
		try (final BufferedInputStream stream = new BufferedInputStream(new FileInputStream(aFile))) {
			final Crc64 crc = new Crc64();
			int read = -1;
			while ((read = stream.read(buf)) != -1) {
				crc.update(buf, 0, read);
			}
			return crc.getValue();
		}
	}

	public static boolean hasParentInSources(final Path newSource, final Collection<Path> sources) {
		for (final Path sdir : sources) {
			if (hasParentInSource(newSource, sdir)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasParentInSource(final Path newSource, final Path source) {
		return newSource.equals(source) || newSource.startsWith(source) || source.startsWith(newSource);
	}

	public static boolean hasParentToNewSource(final Path newSource, final Collection<Path> sources) {
		for (final Path sdir : sources) {
			if (sdir.startsWith(newSource) && !sdir.equals(newSource)) {
				return true;
			}
		}
		return false;
	}

	private static void validate(final File rootPath, final File file) {
		if (rootPath == null) {
			throw new NullPointerException("root path cannot be null");
		}
		if (!rootPath.exists()) {
			throw new IllegalStateException("root path does not exist " + rootPath);
		}
		if (!rootPath.isDirectory()) {
			throw new IllegalStateException("root path must be a directory " + rootPath);
		}
		if (file == null) {
			throw new NullPointerException("file cannot be null");
		}
		if (!file.exists()) {
			throw new IllegalStateException("file does not exist " + file);
		}
		if (file.isDirectory()) {
			throw new IllegalStateException("file cannot be a directory " + file);
		}
	}

}
