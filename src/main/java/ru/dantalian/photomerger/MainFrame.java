package ru.dantalian.photomerger;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainFrame {
	
	private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
	
	private JFrame frame;
	
	public void init() {
		logger.info("Init main frame");
		this.frame = new JFrame("FileList");
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setSize(400, 400);
		this.frame.getContentPane().add(new ListDemo());
		this.frame.setVisible(true);
	}

}
