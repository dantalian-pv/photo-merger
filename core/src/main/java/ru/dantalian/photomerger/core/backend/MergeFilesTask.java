package ru.dantalian.photomerger.core.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.core.AbstractExecutionTask;
import ru.dantalian.photomerger.core.TaskExecutionException;
import ru.dantalian.photomerger.core.events.MergeFilesEvent;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;
import ru.dantalian.photomerger.core.model.FileItem;
import ru.dantalian.photomerger.core.utils.FileItemUtils;

public class MergeFilesTask extends AbstractExecutionTask<Long> {

	private static final Logger logger = LoggerFactory.getLogger(MergeFilesTask.class);

	private final EventManager events;
	
	private final DirItem targetDir;

	private final DirItem metadataFile;
	
	private final boolean copy;

	private final boolean keepPath;

	private final long totalCount;

	private final AtomicLong filesCount = new AtomicLong(0);

	public MergeFilesTask(final DirItem targetDir,
			final DirItem metadataFile, boolean copy, boolean keepPath, long totalCount,
			final EventManager events) {
		this.targetDir = targetDir;
		this.metadataFile = metadataFile;
		this.copy = copy;
		this.keepPath = keepPath;
		this.totalCount = totalCount;
		this.events = events;
	}

	protected List<Future<Long>> execute0() throws TaskExecutionException {
		long duplicates = 0;
		try (final BufferedReader reader = new BufferedReader(new FileReader(metadataFile.getDir()))) {
			String line1 = null;
			String line2 = null;
			while (true) {
				if (this.interrupted.get()) {
					return Collections.singletonList(CompletableFuture.completedFuture(duplicates));
				}
				line1 = (line1 == null) ? reader.readLine() : line1;
				// Prevent reading after EOF
				if (line1 != null) {
					line2 = (line2 == null) ? reader.readLine() : line2;
				}
				FileItem item1 = (line1 != null) ? FileItemUtils.createFileItem(line1, false) : null;
				FileItem item2 = (line2 != null) ? FileItemUtils.createFileItem(line2, false) : null;
				if (item1 != null && item2 != null) {
					// Compare
					// Potentially can be multiple duplicates
					final Map<Long, List<FileItem>> candidates = new HashMap<>();
					while (item1.compareTo(item2) == 0) {
						if (candidates.isEmpty()) {
							// Add only if it's first entry
							final Long crc = FileItemUtils.calculateChecksum(new File(item1.getPath()));
							final FileItem itemCrc = new FileItem(item1.getRootPath(), item1.getPath(), crc, item1.getSize());
							candidates.put(crc, new LinkedList<>(Collections.singletonList(itemCrc)));
						}
						final Long crc = FileItemUtils.calculateChecksum(new File(item2.getPath()));
						final FileItem itemCrc = new FileItem(item2.getRootPath(), item2.getPath(), crc, item2.getSize());
						candidates.putIfAbsent(crc, new LinkedList<>());
						candidates.get(crc).add(itemCrc);
						line2 = reader.readLine();
						item2 = (line2 != null) ? FileItemUtils.createFileItem(line2, false): null;
					}
					if (!candidates.isEmpty()) {
						for(final Entry<Long, List<FileItem>> entry: candidates.entrySet()) {
							final List<FileItem> list = entry.getValue();
							for (int i = 0; i < list.size(); i++) {
								if (i == 0) {
									// Copy/move only one file
									copyMoveFile(list.get(0), copy, keepPath);
								} else {
									duplicates++;
									logger.info("Found duplicate {} and {}", list.get(0), list.get(i));
									this.events.publish(new MergeFilesEvent(this.filesCount.incrementAndGet(), totalCount));
								}
							}
						}
					}
					if (line2 == null) {
						// Reached EOF
						return Collections.singletonList(CompletableFuture.completedFuture(duplicates));
					}
					line1 = line2;
					line2 = null;
				} else if (item1 != null) {
					copyMoveFile(item1, copy, keepPath);
					line1 = null;
					line2 = null;
				} else if (item2 != null) {
					copyMoveFile(item2, copy, keepPath);
					line1 = null;
					line2 = null;
				} else {
					return Collections.singletonList(CompletableFuture.completedFuture(duplicates));
				}
			}
		} catch (IOException e) {
			throw new TaskExecutionException("Failed to merge files", e);
		} finally {
			// Delete metadata directory and its content
			final File metadataDir = metadataFile.getDir().getParentFile();
			if (metadataDir.getName().equals(".metadata")) {
				try {
					Files.walkFileTree(metadataDir.toPath(), new DeleteFileVisitor());
				} catch (final IOException e) {
					logger.error("Failed to remove temporary metadata files", e);
				}
			}
		}
	}

	private void copyMoveFile(final FileItem item, final boolean copy, final boolean keepPath) throws IOException {
		if (this.interrupted.get()) {
			return;
		}
		Path target = this.targetDir.getDir().toPath();
		if (keepPath) {
			String subpath = item.getPath().replace(item.getRootPath(), "");
			if (subpath.startsWith("/")) {
				subpath = subpath.substring(1);
			}
			target = target.resolve(subpath);
		} else {
			target = target.resolve(new File(item.getPath()).getName());
		}
		Path source = Paths.get(item.getPath());
		if (source.equals(target)) {
			return;
		}
		if (target.toFile().exists()) {
			logger.debug("Target file already exists {}", target);
			// Make additional check by comparing crc
			FileItem targetItem = FileItemUtils.createFileItem(this.targetDir.getDir(), target.toFile(), true);
			FileItem srcItem = item.getCrc() == 0 ?
					FileItemUtils.createFileItem(new File(item.getRootPath()), source.toFile(), true)
					: item;
			if (targetItem.compareTo(srcItem) == 0) {
				// Nothing to do, same file
				return;
			} else {
				// Different files. Need to copy to a new name
				int i = 1;
				while(target.toFile().exists()) {
					target = target.resolveSibling(target.getFileName() + "__" + i);
					i++;
				}
			}
		}
		if (target.getParent() != null) {
			final File dir = target.getParent().toFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		if (copy) {
			Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
		} else {
			Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
		}
		this.events.publish(new MergeFilesEvent(this.filesCount.incrementAndGet(), totalCount));
	}

}
