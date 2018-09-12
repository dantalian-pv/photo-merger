package ru.dantalian.photomerger.core.backend.commands;

import java.nio.file.FileVisitOption;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import ru.dantalian.photomerger.core.backend.FileTreeWalker;
import ru.dantalian.photomerger.core.backend.OnlyFileVisitor;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;

public class StoreMetadataCommand implements Callable<List<DirItem>> {
	
	private final DirItem sourceDir;

	private final DirItem targetDir;

	private final AtomicBoolean interrupted;

	private final FileTreeWalker fileTreeWalker;

	private final EventManager events;

	private final AtomicLong filesCount;

	private final long totalCount;

	public StoreMetadataCommand(final DirItem sourceDir, final DirItem targetDir,
			final FileTreeWalker fileTreeWalker,
			final EventManager events,
			final AtomicLong filesCount,
			final long totalCount,
			final AtomicBoolean interrupted) {
		this.sourceDir = sourceDir;
		this.targetDir = targetDir;
		this.fileTreeWalker = fileTreeWalker;
		this.events = events;
		this.filesCount = filesCount;
		this.totalCount = totalCount;
		this.interrupted = interrupted;
	}

	@Override
	public List<DirItem> call() throws Exception {
		final List<DirItem> metadataFiles = new LinkedList<>();
		try (final WriteMetadataCommand command = new WriteMetadataCommand(
				this.sourceDir,
				this.targetDir,
				metadataFiles,
				this.events,
				this.filesCount,
				this.totalCount)) {
			this.fileTreeWalker.walkFileTree(this.sourceDir.getDir().toPath(),
					Collections.singleton(FileVisitOption.FOLLOW_LINKS),
					Integer.MAX_VALUE,
					new OnlyFileVisitor(this.interrupted, command));
		}
		return metadataFiles;
	}

}