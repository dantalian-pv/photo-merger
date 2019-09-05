package ru.dantalian.photomerger.cli;

import java.io.File;
import java.util.List;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import ru.dantalian.photomerger.core.MergeAction;

@Command(name = "photo-merger-cli")
public class CliOptions {

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Display help")
	private boolean help;

	@Option(names = { "-t", "--target" }, paramLabel = "DIR", required = true, description = "Target folder")
	private File target;

	@Option(names = { "-a", "--action" }, showDefaultValue = Visibility.ALWAYS, defaultValue = "COPY",
			description = { "Copy/Move unique files to target folder", "Or Delete dublicates from sources", "Values: COPY, MOVE, DELETE" })
	private MergeAction action = MergeAction.COPY;

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

	public MergeAction getAction() {
		return action;
	}

	public boolean isKeepPath() {
		return keepPath;
	}

	public List<File> getSource() {
		return source;
	}

}
