package ru.dantalian.photomerger.core.backend;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.util.Set;

public interface FileTreeWalker {
	
	Path walkFileTree(Path start, Set<FileVisitOption> options,
			int maxDepth, FileVisitor<? super Path> visitor) throws IOException ;

}
