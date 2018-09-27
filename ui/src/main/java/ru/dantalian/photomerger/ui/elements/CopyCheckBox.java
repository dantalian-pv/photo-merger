package ru.dantalian.photomerger.ui.elements;

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class CopyCheckBox {

	private Button copyCheckBox;

	public CopyCheckBox(final Composite parent, final ResourceBundle messages) {
		this.copyCheckBox = new Button(parent, SWT.CHECK);

		this.copyCheckBox.setText(messages.getString(InterfaceStrings.COPY));
		this.copyCheckBox.setSelection(true);
		
		this.copyCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				copyCheckBox.setText((copyCheckBox.getSelection())
						? messages.getString(InterfaceStrings.COPY) : messages.getString(InterfaceStrings.MOVE));
				copyCheckBox.getParent().layout();
			}
		});
	}

	public Button getButton() {
		return copyCheckBox;
	}

}
