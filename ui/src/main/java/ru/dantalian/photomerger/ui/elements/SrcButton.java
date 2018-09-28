package ru.dantalian.photomerger.ui.elements;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.core.utils.FileItemUtils;

public class SrcButton {

	private static final Logger logger = LoggerFactory.getLogger(SrcButton.class);

	private final Button button;

	public SrcButton(final Composite parent, final Shell shell, final List list,
			final ResourceBundle messages) {
		this.button = new Button(parent, SWT.PUSH);

		this.button.setText(messages.getString(InterfaceStrings.ADD));

		this.button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final DirectoryDialog dd = new DirectoryDialog(shell, SWT.APPLICATION_MODAL);
				dd.setMessage(messages.getString(InterfaceStrings.ADD_SOURCE));
				dd.setText(messages.getString(InterfaceStrings.SET_DIR));
				final String string = dd.open();
				if (string == null) {
					// No source was selected
					return;
				}
				final Path newSource = Paths.get(string);
				final java.util.List<Path> items = Arrays.asList(list.getItems())
						.stream()
						.map(aItem -> Paths.get(aItem))
						.collect(Collectors.toList());
				final boolean found = FileItemUtils.hasParentInSources(newSource, items);
				if (found) {
					final boolean parent = FileItemUtils.hasParentToNewSource(newSource, items);
					if (parent) {
						final MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);

						messageBox.setText(messages.getString(InterfaceStrings.PARENT_DIR));
						messageBox.setMessage(messages.getString(InterfaceStrings.REPLACE_WITH_PARENT));
						final int buttonID = messageBox.open();
						if (buttonID == SWT.YES) {
							// Remove all children directories and then add parent
							for (int i = items.size() - 1; i >= 0; i--) {
								final Path sdir = items.get(i);
								if (sdir.startsWith(newSource)) {
									list.remove(i);
								}
							}
							logger.info("add {}", string);
							list.add(string);
						}
					}
				} else {
					logger.info("add {}", string);
					list.add(string);
				}
			}

		});
	}

	public Button getButton() {
		return button;
	}

}
