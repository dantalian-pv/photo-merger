package ru.dantalian.photomerger.ui.elements;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class SrcDirsList {

	private List list;

	public SrcDirsList(final Composite parent) {
		this.list = new List(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);

		this.list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.list.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					final int[] selectionIndices = list.getSelectionIndices();
					list.remove(selectionIndices);
				}
			}
		});
	}

	public List getList() {
		return list;
	}

}
