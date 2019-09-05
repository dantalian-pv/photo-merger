package ru.dantalian.photomerger.ui.elements;

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class ActionComboBox {

	private final Combo actionBox;

	public ActionComboBox(final Composite parent, final ResourceBundle messages) {
		this.actionBox = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.actionBox.setItems(new String[] {
				messages.getString(InterfaceStrings.COPY),
				messages.getString(InterfaceStrings.MOVE),
				messages.getString(InterfaceStrings.DELETE)
		});
		this.actionBox.select(0);
	}

	public Combo getBox() {
		return actionBox;
	}

}
