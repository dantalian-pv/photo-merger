package ru.dantalian.photomerger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenButtonListener implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(OpenButtonListener.class);

	private final DefaultListModel listModel;

	private JFileChooser fc;

	public OpenButtonListener(DefaultListModel listModel) {
		super();
		this.listModel = listModel;
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		int retVal = fc.showOpenDialog(fc);
		if (retVal == JFileChooser.APPROVE_OPTION) {
			listModel.clear();

			File dir = fc.getSelectedFile();
			logger.info("add {}", dir);
			listModel.addElement("[" + dir.getPath() + "]");
			for (File file : dir.listFiles()) {
				listModel.addElement(file.getName());
			}

		}
	}

}
