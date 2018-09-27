package ru.dantalian.photomerger.ui.elements;

import java.io.File;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.ui.backend.TaskTrigger;

public class TargetButton {

	private final Button button;

	public TargetButton(final Composite parent, final Shell shell, final List list,
			final TaskTrigger taskTrigger, final ResourceBundle messages) {
		this.button = new Button(parent, SWT.PUSH);

		this.button.setText(messages.getString(InterfaceStrings.START));
		this.button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (taskTrigger.isStarted()) {
					taskTrigger.startStop(false, null);
				} else {
					if (list.getItemCount() == 0) {
						final MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(messages.getString(InterfaceStrings.ERROR_TITLE));
						messageBox.setMessage(messages.getString(InterfaceStrings.NO_SOURCE));
						messageBox.open();
						return;
					}
					while (true) {
						final DirectoryDialog dd = new DirectoryDialog(shell, SWT.APPLICATION_MODAL);
						dd.setMessage(messages.getString(InterfaceStrings.SET_TARGET));
						dd.setText(messages.getString(InterfaceStrings.SET_DIR));
						final String dir = dd.open();
						if (dir != null) {
							final String tdir = dir + "/";
							final String[] items = list.getItems();
							boolean err = false;
							for (String sdir : items) {
								sdir += "/";
								if (tdir.equals(sdir) || tdir.contains(sdir) || sdir.contains(tdir)) {
									final MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
									messageBox.setText(messages.getString(InterfaceStrings.ERROR_TITLE));
									messageBox.setMessage(messages.getString(InterfaceStrings.DIFFER_TARGET));
									messageBox.open();
									err = true;
									break;
								}
							}
							if (err) {
								continue;
							}
							taskTrigger.startStop(true, new DirItem(new File(dir)));
							return;
						} else {
							return;
						}
					}
				}
			}
		});
	}

	public Button getButton() {
		return button;
	}

}
