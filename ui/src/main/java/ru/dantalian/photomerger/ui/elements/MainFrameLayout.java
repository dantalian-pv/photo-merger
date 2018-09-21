package ru.dantalian.photomerger.ui.elements;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainFrameLayout {

	private static final Logger logger = LoggerFactory.getLogger(MainFrameLayout.class);

	private final GridLayout frame;

	public MainFrameLayout(final Shell shell) {
		frame = new GridLayout(1, false);
		frame.marginWidth = 0;
		frame.marginHeight = 0;
		frame.verticalSpacing = 0;
		frame.horizontalSpacing = 0;

		Composite top = new Composite(shell, SWT.NONE);
		GridData d1 = new GridData(SWT.FILL, SWT.FILL, true, true);
		top.setLayoutData(d1);
		top.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));

		createLayer(shell);
		createLayer(shell);
		createLayer(shell);
	}

	public Layout getFrame() {
		return frame;
	}

	private static Composite createLayer(final Composite parent) {

		Composite layer = new Composite(parent, SWT.NONE);
		layer.setLayout(new FillLayout());
		for (int i = 0; i < 10; i++) {
			Label label = new Label(layer, SWT.NONE);
			label.setText("I go \u26F7");
			label.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseDown(MouseEvent e) {
					Shell shell = Display.getDefault().getActiveShell();
					MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
					dialog.setText("My info");
					dialog.setMessage("Do you really want to do this?");
					dialog.open();

				}

			});
		}
		Button removeButton = new Button(layer, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				layer.dispose();
				parent.requestLayout();
				// DirectoryDialog dd = new DirectoryDialog((Shell) parent, SWT.APPLICATION_MODAL);
				// dd.setMessage("Choose");
				// dd.setText("text");
				// String string = dd.open();
				// logger.info(string);
			}
		});

		Button addButton = new Button(layer, SWT.PUSH);
		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Composite composite = createLayer(parent);
				composite.moveAbove(addButton.getParent());
				parent.requestLayout();

			}
		});

		GridData d2 = new GridData(SWT.FILL, SWT.TOP, true, false);
		layer.setLayoutData(d2);
		return layer;
	}

}
