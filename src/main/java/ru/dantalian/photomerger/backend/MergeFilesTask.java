package ru.dantalian.photomerger.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.ProgressStateManager;
import ru.dantalian.photomerger.model.DirItem;
import ru.dantalian.photomerger.model.FileItem;
import ru.dantalian.photomerger.utils.FileItemUtils;

public class MergeFilesTask {
	
	private static final Logger logger = LoggerFactory.getLogger(MergeFilesTask.class);

	private final ProgressStateManager progress;
	
	private final DirItem targetDir;
	
	private final boolean copy;

	private final boolean keepPath;

	private final long totalCount;

	private final AtomicLong filesCount = new AtomicLong(0);
	
	private final Timer timer = new Timer("store-metadata-progress", true);

	public MergeFilesTask(final ProgressStateManager progress, final DirItem targetDir,
			boolean copy, boolean keepPath, long totalCount) {
		this.progress = progress;
		this.targetDir = targetDir;
		this.copy = copy;
		this.keepPath = keepPath;
		this.totalCount = totalCount;
	}

	public void mergeFiles(final DirItem metadataFile) throws IOException {
		filesCount.set(0);

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (progress.isStarted() && filesCount.get() > 0L) {
					progress.setProgressText("Merging files");
					final int percent = 66 + (int) (filesCount.get() * 33L / totalCount);
					progress.setCurrent("" + filesCount.get(), percent);
					progress.setMax("" + totalCount);
				}
			}
		}, 1000, 1000);
		
		try (final BufferedReader reader = new BufferedReader(new FileReader(metadataFile.getDir()))) {
			String line1 = null;
			String line2 = null;
			while (true) {
				if (!progress.isStarted()) {
					return;
				}
				line1 = (line1 == null) ? reader.readLine() : line1;
				// Prevent reading after EOF
				if (line1 != null) {
					line2 = (line2 == null) ? reader.readLine() : line2;
				}
				FileItem item1 = (line1 != null) ? FileItemUtils.createFileItem(line1) : null;
				FileItem item2 = (line2 != null) ? FileItemUtils.createFileItem(line2) : null;
				if (item1 != null && item2 != null) {
					// Compare
					// Potentially can be multiple duplicates
					while (item1.compareTo(item2) == 0) {
						// Same files copy only one
						logger.info("Found duplicate {} and {}", item1, item2);
						line2 = reader.readLine();
						item2 = (line2 != null) ? FileItemUtils.createFileItem(line2): null;
					}
					// Copy/move only one file
					copyMoveFile(item1, copy, keepPath);
					if (line2 == null) {
						// Reached EOF
						return;
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
					return;
				}
			}
		} finally {
			metadataFile.getDir().delete();
		}
	}

	private void copyMoveFile(final FileItem item, final boolean copy, final boolean keepPath) throws IOException {
		if (!progress.isStarted()) {
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
			FileItem targetItem = FileItemUtils.createFileItem(this.targetDir.getDir(), target.toFile());
			if (targetItem.compareTo(item) == 0) {
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
		final File dir = target.getParent().toFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (copy) {
			Files.copy(source, target);
		} else {
			Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.COPY_ATTRIBUTES);
		}
		filesCount.incrementAndGet();
	}

	public void finish() {
		timer.cancel();
	}

}
