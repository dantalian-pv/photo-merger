package ru.dantalian.photomerger.core.backend.commands;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.core.CoreConstants;
import ru.dantalian.photomerger.core.backend.VisitSingleFileCommand;
import ru.dantalian.photomerger.core.events.StoreMetadataEvent;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;
import ru.dantalian.photomerger.core.model.FileItem;
import ru.dantalian.photomerger.core.utils.FileItemUtils;
import ru.dantalian.photomerger.core.utils.UUIDGenerator;

public class WriteMetadataCommand implements VisitSingleFileCommand, AutoCloseable {
	
	private static final Logger logger = LoggerFactory.getLogger(WriteMetadataCommand.class);

	private static final int LIMIT = 1000;

	private final DirItem sourceDir;

	private final DirItem targetDir;

	private final List<DirItem> metadataFiles;

	private final EventManager events;

	private final AtomicLong filesCount;

	private final long totalCount;

	private final List<FileItem> queue = new ArrayList<>(LIMIT);

	public WriteMetadataCommand(final DirItem sourceDir, final DirItem targetDir,
			final List<DirItem> metadataFiles,
			final EventManager events,
			final AtomicLong filesCount, final long totalCount) {
		this.sourceDir = sourceDir;
		this.targetDir = targetDir;
		this.metadataFiles = metadataFiles;
		this.events = events;
		this.filesCount = filesCount;
		this.totalCount = totalCount;
	}

	@Override
	public void visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
		if (this.queue.size() >= LIMIT) {
			writeData();
		}
		final FileItem fileItem = FileItemUtils.createFileItem(this.sourceDir.getDir(), file.toFile(), false);
		this.queue.add(fileItem);
		events.publish(new StoreMetadataEvent(this.filesCount.incrementAndGet(), this.totalCount));
	}

	@Override
	public void close() throws Exception {
		if (!this.queue.isEmpty()) {
			writeData();
		}
	}

	private void writeData() throws IOException {
		final Path metadataPath = getMetadataPath(targetDir);
		logger.info("Storing metadata for {} to {} files = {}", sourceDir, metadataPath, this.queue.size());
		Collections.sort(this.queue);
		try (final PrintWriter writer = new PrintWriter(new BufferedOutputStream(
				new FileOutputStream(metadataPath.toFile())))) {
			for (final FileItem fileItem: this.queue) {
				writer.println(FileItemUtils.externalize(fileItem));
			}
		} finally {
			this.queue.clear();
			this.metadataFiles.add(new DirItem(metadataPath.toFile()));
		}
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
