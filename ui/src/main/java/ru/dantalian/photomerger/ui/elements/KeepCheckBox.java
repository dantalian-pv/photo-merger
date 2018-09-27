package ru.dantalian.photomerger.ui.elements;

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class KeepCheckBox {
	
	private Button keepCheckBox;

	public KeepCheckBox(final Composite parent, final ResourceBundle messages) {
		this.keepCheckBox = new Button(parent, SWT.CHECK);

		this.keepCheckBox.setText(messages.getString(InterfaceStrings.KEEP_PATH));
		this.keepCheckBox.setSelection(true);
	}

	public Button getButton() {
		return keepCheckBox;
	}

}
