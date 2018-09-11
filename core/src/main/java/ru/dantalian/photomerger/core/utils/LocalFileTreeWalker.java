package ru.dantalian.photomerger.core.utils;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import ru.dantalian.photomerger.core.backend.FileTreeWalker;

public class LocalFileTreeWalker implements FileTreeWalker {

	@Override
	public Path walkFileTree(final Path start, final Set<FileVisitOption> options,
			final int maxDepth, final FileVisitor<? super Path> visitor) throws IOException {
		return Files.walkFileTree(start, options, maxDepth, visitor);
	}

}
