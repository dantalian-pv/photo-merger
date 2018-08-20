package ru.dantalian.photomerger.core.backend;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public interface VisitSingleFileCommand {
	
	void visitFile(Path file, BasicFileAttributes attrs) throws IOException;

}
