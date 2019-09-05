package ru.dantalian.photomerger.cli;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;

public class PhotoMergerMain {

	private static final Logger logger = LoggerFactory.getLogger(PhotoMergerMain.class);

	public static void main(final String[] args) {
		new PhotoMergerMain().run(args);
	}

	public void run(final String[] args) {
		if (args == null || args.length == 0) {
			CommandLine.usage(new CliOptions(), System.out);
			return;
		}
		try {
		final CliOptions cliOptions = CommandLine.populateCommand(new CliOptions(), args);
		if (cliOptions.isHelp()) {
			CommandLine.usage(new CliOptions(), System.out);
			return;
		}
		printSettings(cliOptions);
		validate(cliOptions.getTarget(), cliOptions.getSource());
		new TasksExecutor().execute(cliOptions.getAction(),
				cliOptions.isKeepPath(),
				cliOptions.getTarget(),
				cliOptions.getSource());
		} catch (final MissingParameterException e) {
			System.err.println(e.getMessage());
			CommandLine.usage(new CliOptions(), System.out);
		} catch (final CliException e) {
			logger.error(e.getMessage(), e);
			System.err.println(e.getMessage());
		}
	}

	private void validate(final File target, final List<File> source) throws CliException {
		// Check if all present dirs are exist
		validateDir(target);
		for (final File sdir: source) {
			validateDir(sdir);
		}
		final Path tdir = target.toPath();
		final List<Path> sdirs = source.stream()
				.map(aItem -> aItem.toPath())
				.collect(Collectors.toList());
		checkParent(tdir, sdirs);
		// Also check all sources if one of them is parent to any other
		final Iterator<Path> iterator = sdirs.iterator();
		while (iterator.hasNext()) {
			final Path toCheck = iterator.next();
			iterator.remove();
			checkParent(toCheck, sdirs);
		}
	}

	private void validateDir(final File target) throws CliException {
		if (!target.exists()) {
			throw new CliException("dir does not exist " + target.getPath());
		}
		if (!target.isDirectory()) {
			throw new CliException("must be a directory " + target.getPath());
		}
	}

	private void checkParent(final Path tdir, final List<Path> sdirs) throws CliException {
		for (final Path sdir : sdirs) {
			if (tdir.startsWith(sdir)) {
				throw new CliException("Folder: " + sdir + " is parent or equal to: " + tdir);
			}
			if (sdir.startsWith(tdir)) {
				throw new CliException("Folder: " + tdir + " is parent or equal to: " + sdir);
			}
		}
	}

	private void printSettings(final CliOptions cliOptions) {
		System.out.println("Current settings:");
		System.out.println("action = " + cliOptions.getAction());
		System.out.println("keep = " + cliOptions.isKeepPath());
		System.out.println("src = " + cliOptions.getSource());
		System.out.println("target = " + cliOptions.getTarget());
	}

}
