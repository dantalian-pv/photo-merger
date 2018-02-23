package ru.dantalian.photomerger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.model.DirItem;

public class SelectSourceFolder extends JButton implements ActionListener {

	private static final long serialVersionUID = 7860567815750245577L;

	private static final Logger logger = LoggerFactory.getLogger(SelectSourceFolder.class);

	private final DefaultListModel<DirItem> listModel;

	private final JFileChooser fc;

	public SelectSourceFolder(final String text, final DefaultListModel<DirItem> listModel) {
		super(text);
		super.setActionCommand(text);
		super.addActionListener(this);
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
				Enumeration<DirItem> en = listModel.elements();
				boolean found = false;
				while (en.hasMoreElements()) {
					final String sdir = en.nextElement().getPath();
					if (s.contains(sdir)) {
						found = true;
						break;
					}
				}
				if (!found) {
					logger.info("add {}", dir);
					listModel.addElement(new DirItem(dir));
				}
			}
		}
	}

}
