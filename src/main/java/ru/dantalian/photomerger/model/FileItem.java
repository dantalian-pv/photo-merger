package ru.dantalian.photomerger.model;

public class FileItem implements Comparable<FileItem> {

	private final String rootPath;

	private final String path;

	private final long crc;

	public FileItem(final String rootPath, final String path, final long crc) {
		this.rootPath = rootPath;
		this.path = path;
		this.crc = crc;
	}

	public String getRootPath() {
		return rootPath;
	}

	public String getPath() {
		return path;
	}

	public long getCrc() {
		return crc;
	}

	@Override
	public int compareTo(final FileItem o) {
		return Long.compare(this.crc, o.crc);
	}

	@Override
	public String toString() {
		return "FileItem [rootPath=" + rootPath + ", path=" + path + ", crc=" + crc + "]";
	}

}
