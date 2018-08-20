package ru.dantalian.photomerger.core.model;

public class FileItem implements Comparable<FileItem> {

	private final String rootPath;

	private final String path;

	private final long crc;

	private final long size;

	public FileItem(final String rootPath, final String path, final long crc,
			final long size) {
		this.rootPath = rootPath;
		this.path = path;
		this.crc = crc;
		this.size = size;
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
	
	public long getSize() {
		return size;
	}

	@Override
	public int compareTo(final FileItem o) {
		if (o == null) {
			return -1;
		}
		if (this.size == o.size) {
			return Long.compare(this.crc, o.crc);
		}
		return Long.compare(this.size, o.size);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((rootPath == null) ? 0 : rootPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileItem other = (FileItem) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (rootPath == null) {
			if (other.rootPath != null)
				return false;
		} else if (!rootPath.equals(other.rootPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FileItem [rootPath=" + rootPath + ", path=" + path + ", crc=" + crc + ", size=" + size + "]";
	}

}
