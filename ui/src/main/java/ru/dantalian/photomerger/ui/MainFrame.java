package ru.dantalian.photomerger.ui;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.ui.elements.MainFrameLayout;

public class MainFrame {
	
	private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);

	private final GridLayout frame;
	
	public MainFrame() {
		this.frame = new GridLayout(1, true);
		this.frame.marginWidth = 0;
		this.frame.marginHeight = 0;
		this.frame.verticalSpacing = 0;
		this.frame.horizontalSpacing = 0;
	}
	
	public void init() {
		logger.info("Init main frame");

		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("Photo Merger");
		shell.setMinimumSize(524, 400);

		new MainFrameLayout(shell);
		shell.setLayout(this.frame);
		
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}

}
