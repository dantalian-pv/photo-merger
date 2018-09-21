package ru.dantalian.photomerger.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.ui.elements.MainFrameLayout;

public class MainFrame {
	
	private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
	
	public void init() {
		logger.info("Init main frame");
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("Photo Merger");
		shell.setMinimumSize(400, 400);

		final MainFrameLayout windowLayout = new MainFrameLayout(shell);
		shell.setLayout(windowLayout.getFrame());
		
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}

}
