package ru.dantalian.photomerger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectTargetFolder extends JButton implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(SelectTargetFolder.class);

	private final DefaultListModel listModel;
	
	private final ProgressStateManager progressStateManager;

	private JFileChooser fc;

	public SelectTargetFolder(final String text,
			final DefaultListModel listModel,
			final ProgressStateManager progressStateManager) {
		super(text);
		super.setActionCommand(text);
		super.addActionListener(this);
		this.listModel = listModel;
		this.progressStateManager = progressStateManager;
		this.fc = new JFileChooser();
		this.fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (this.progressStateManager.isStarted()) {
			this.progressStateManager.stopProcess();
		} else {
			if (listModel.isEmpty()) {
				JOptionPane.showMessageDialog(this.getParent().getParent().getParent(),
				    "No source dir is selected. Add at lest one source dir.",
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
				return;
			}
			int retVal = this.fc.showOpenDialog(this.fc);
			if (retVal == JFileChooser.APPROVE_OPTION) {
				String dir = fc.getSelectedFile().getPath();
				Enumeration en = listModel.elements();
				while (en.hasMoreElements()) {
					final String sdir = (String) en.nextElement();
					if (dir.equals(sdir) || dir.contains(sdir) || sdir.contains(dir)) {
						JOptionPane.showMessageDialog(this.getParent().getParent().getParent(),
						    "Target dir should be different than source dirs",
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				this.progressStateManager.startProcess();
			}
			
		}
	}

}
