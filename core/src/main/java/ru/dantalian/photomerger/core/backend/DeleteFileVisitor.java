package ru.dantalian.photomerger.core.backend;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class DeleteFileVisitor extends SimpleFileVisitor<Path> {

	@Override
	public FileVisitResult visitFile(final Path aFile, final BasicFileAttributes aAttrs) throws IOException {
		Files.deleteIfExists(aFile);
		return super.visitFile(aFile, aAttrs);
	}

	@Override
	public FileVisitResult postVisitDirectory(final Path aDir, final IOException aExc) throws IOException {
		Files.deleteIfExists(aDir);
		return super.postVisitDirectory(aDir, aExc);
	}

}
