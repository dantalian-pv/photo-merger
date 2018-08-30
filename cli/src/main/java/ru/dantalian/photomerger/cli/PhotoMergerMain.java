package ru.dantalian.photomerger.cli;

import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;

public class PhotoMergerMain {

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
		new TasksExecutor().execute(cliOptions.isCopy(),
				cliOptions.isKeepPath(),
				cliOptions.getTarget(),
				cliOptions.getSource());
		} catch (final MissingParameterException e) {
			System.err.println(e.getMessage());
			CommandLine.usage(new CliOptions(), System.out);
		}
	}

	private void printSettings(final CliOptions cliOptions) {
		System.out.println("Current settings:");
		System.out.println("copy = " + cliOptions.isCopy());
		System.out.println("keep = " + cliOptions.isKeepPath());
		System.out.println("src = " + cliOptions.getSource());
		System.out.println("target = " + cliOptions.getTarget());
	}

}
