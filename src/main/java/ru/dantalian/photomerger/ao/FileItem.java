package ru.dantalian.photomerger.ao;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ru.dantalian.photomerger.utils.Crc64;

public class FileItem implements Comparable<FileItem> {

	private final File rootPath;

	private final File file;

	private final String path;

	private volatile long crc;

	public FileItem(final File rootPath, final File file) {
		validate(rootPath, file);
		this.rootPath = rootPath;
		this.file = file;
		this.path = file.getPath();
	}

	public FileItem(final String checksumAndPath) {
		final String[] split = checksumAndPath.split(":");
		if (split.length != 3) {
			throw new IllegalArgumentException("Wrong FileItem format " + checksumAndPath);
		}
		this.rootPath = new File(split[1]);
		this.file = new File(split[2]);
		this.path = this.file.getPath();
		this.crc = Long.parseLong(split[0]);
		validate(this.rootPath, this.file);
	}

	public File getRootPath() {
		return rootPath;
	}

	public File getFile() {
		return file;
	}

	public long getChecksum() throws IOException {
		if (this.crc == 0) {
			this.crc = calculateChecksum();
		}
		return this.crc;
	}

	public String getHexChecksum() throws IOException {
		return String.format("%1x", Long.valueOf(this.getChecksum()));
	}

	@Override
	public int compareTo(final FileItem o) {
		try {
			return Long.compare(this.getChecksum(), o.getChecksum());
		} catch (IOException e) {
			return 10;
		}
	}

	@Override
	public String toString() {
		return "FileItem [rootPath=" + rootPath + ", path=" + path + ", file=" + file + ", crc=" + crc + "]";
	}

	private long calculateChecksum() throws IOException {
		final byte[] buf = new byte[4096];
		try (final BufferedInputStream stream = new BufferedInputStream(
				new FileInputStream(this.file))) {
			final Crc64 crc = new Crc64();
			int read = -1;
			while ((read = stream.read(buf)) != -1) {
				crc.update(buf, 0, read);
			}
			return crc.getValue();
		}
	}

	private void validate(final File rootPath, final File file) {
		if (rootPath == null || !rootPath.exists() || !rootPath.isDirectory()) {
			throw new IllegalArgumentException("root path should not be null " + file);
		}
		if (file == null || !file.exists() || file.isDirectory()) {
			throw new IllegalArgumentException("Wrong file " + file);
		}
	}

}
