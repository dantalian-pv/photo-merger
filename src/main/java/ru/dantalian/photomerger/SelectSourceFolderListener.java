package ru.dantalian.photomerger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectSourceFolderListener implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(SelectSourceFolderListener.class);

	private final DefaultListModel listModel;

	private JFileChooser fc;

	public SelectSourceFolderListener(DefaultListModel listModel) {
		super();
		this.listModel = listModel;
		fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		int retVal = fc.showOpenDialog(fc);
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File[] dirs = fc.getSelectedFiles();
			for (File dir: dirs) {
				String s = dir.getPath();
				Enumeration en = listModel.elements();
				boolean found = false;
				while (en.hasMoreElements()) {
					final String sdir = (String) en.nextElement();
					if (s.contains(sdir)) {
						found = true;
						break;
					}
				}
				if (!found) {
					logger.info("add {}", dir);
					listModel.addElement(dir.getPath());
				}
			}
		}
	}

}
