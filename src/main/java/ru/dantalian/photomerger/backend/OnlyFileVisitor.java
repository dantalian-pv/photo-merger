package ru.dantalian.photomerger.backend;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import ru.dantalian.photomerger.ProgressStateManager;

public class OnlyFileVisitor implements FileVisitor<Path> {

	private final ProgressStateManager progress;

	private final VisitSingleFileCommand command;

	public OnlyFileVisitor(final ProgressStateManager progress, final VisitSingleFileCommand command) {
		this.progress = progress;
		this.command = command;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if (!progress.isStarted()) {
			return FileVisitResult.TERMINATE;
		}
		if (dir.toFile().isHidden()) {
			return FileVisitResult.SKIP_SUBTREE;
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if (!progress.isStarted()) {
			return FileVisitResult.TERMINATE;
		}
		if (!file.toFile().isHidden() && attrs.isRegularFile()) {
			command.visitFile(file, attrs);
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		if (!progress.isStarted()) {
			return FileVisitResult.TERMINATE;
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		if (!progress.isStarted()) {
			return FileVisitResult.TERMINATE;
		}
		return FileVisitResult.CONTINUE;
	}

}
