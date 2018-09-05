package ru.dantalian.photomerger.ui;

import java.awt.Dimension;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.ui.elements.ListPanel;

public class MainFrame {
	
	private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
	
	private JFrame frame;
	
	public void init() {
		logger.info("Init main frame");
		this.frame = new JFrame("Photo Merger");
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setMinimumSize(new Dimension(400, 400));
		this.frame.getContentPane().add(new ListPanel());
		this.frame.setVisible(true);
		this.frame.pack();
	}

}
