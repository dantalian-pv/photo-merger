package ru.dantalian.photomerger.core.backend.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.Callable;

import ru.dantalian.photomerger.core.CoreConstants;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.FileItem;
import ru.dantalian.photomerger.core.utils.FileItemUtils;
import ru.dantalian.photomerger.core.utils.UUIDGenerator;

public class MergeCommand implements Callable<DirItem> {

	private final DirItem left;
	private final DirItem right;
	private final DirItem targetDir;

	public MergeCommand(final DirItem left, final DirItem right, final DirItem targetDir) {
		this.left = left;
		this.right = right;
		this.targetDir = targetDir;
	}

	@Override
	public DirItem call() throws Exception {
		final Path mergedPath = getMetadataPath(this.targetDir);
		try (final BufferedReader leftReader = new BufferedReader(new FileReader(left.getDir()));
				final BufferedReader rightReader = new BufferedReader(new FileReader(right.getDir()));
				final PrintWriter writer = new PrintWriter(new FileWriter(mergedPath.toFile()))) {
			final Iterator<String> leftIterator = leftReader.lines().iterator();
			final Iterator<String> rightIterator = rightReader.lines().iterator();
			FileItem leftItem = null;
			FileItem rightItem = null;
			while (leftIterator.hasNext() || rightIterator.hasNext()) {
				leftItem = (leftIterator.hasNext() && leftItem == null) ? FileItemUtils
						.createFileItem(leftIterator.next(), false) : leftItem;
				rightItem = (rightIterator.hasNext() && rightItem == null) ? FileItemUtils
						.createFileItem(rightIterator.next(), false) : rightItem;
				if (leftItem == null && rightItem != null) {
					writer.println(FileItemUtils.externalize(rightItem));
					rightItem = null;
				} else if (leftItem != null && rightItem == null) {
					writer.println(FileItemUtils.externalize(leftItem));
					leftItem = null;
				} else {
					if (leftItem != null && leftItem.compareTo(rightItem) == 0) {
						writer.println(FileItemUtils.externalize(leftItem));
						leftItem = null;
						writer.println(FileItemUtils.externalize(rightItem));
						rightItem = null;
					} else if (leftItem != null && leftItem.compareTo(rightItem) < 0) {
						writer.println(FileItemUtils.externalize(leftItem));
						leftItem = null;
					} else {
						writer.println(FileItemUtils.externalize(rightItem));
						rightItem = null;
					}
				}
			}
		} finally {
			left.getDir().delete();
			right.getDir().delete();
		}
		return new DirItem(mergedPath.toFile());
	}

	private Path getMetadataPath(final DirItem targetDir) {
		final File metadataDir = targetDir.getDir().toPath()
				.resolve(CoreConstants.METADATA_DIR_NAME).toFile();
		if (!metadataDir.exists()) {
			metadataDir.mkdirs();
		}
		return metadataDir.toPath()
				.resolve(CoreConstants.METADATA_FILE_NAME + "-" + UUIDGenerator.random(8));
	}

}