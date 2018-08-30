package ru.dantalian.photomerger.cli;

import java.io.File;
import java.util.List;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "photo-merger-cli")
public class CliOptions {

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Display help")
	private boolean help;

	@Option(names = { "-t", "--target" }, paramLabel = "DIR", required = true, description = "Target folder")
	private File target;

	@Option(names = { "-c", "--copy" }, showDefaultValue = Visibility.ALWAYS, defaultValue = "true",
			description = { "Move files if 'false'", "Copy files if 'true'", "Use -c=false or --copy=false to change" })
	private boolean copy = true;

	@Option(names = { "-k", "--keep-path" }, showDefaultValue = Visibility.ALWAYS, defaultValue = "true",
			description = { "Flatten all subdirectories if 'false'", "Keep subdirectory structure if 'true'",
					"Use -k=false or --keep-path=false to change" })
	private boolean keepPath = true;

	@Parameters(arity = "1..*", paramLabel = "DIR", description = "Source directories")
	private List<File> source;

	public boolean isHelp() {
		return help;
	}

	public File getTarget() {
		return target;
	}

	public boolean isCopy() {
		return copy;
	}

	public boolean isKeepPath() {
		return keepPath;
	}

	public List<File> getSource() {
		return source;
	}

}
