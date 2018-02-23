package ru.dantalian.photomerger.gui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.ProgressStateManager;
import ru.dantalian.photomerger.model.DirItem;

public class SelectTargetDir extends JButton implements ActionListener {

	private static final long serialVersionUID = 4441106526135161132L;

	private static final Logger logger = LoggerFactory.getLogger(SelectTargetDir.class);

	private final DefaultListModel<DirItem> listModel;

	private final ProgressStateManager progressStateManager;

	private JFileChooser fc;

	public SelectTargetDir(final String text,
			final DefaultListModel<DirItem> listModel,
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
	public void actionPerformed(final ActionEvent e) {
		if (this.progressStateManager.isStarted()) {
			this.progressStateManager.stopProcess();
		} else {
			if (listModel.isEmpty()) {
				JOptionPane.showMessageDialog(this.getRoot(),
						"No source dir is selected. Add at lest one source dir.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			while (true) {
				if (this.fc.showOpenDialog(this.getRoot()) == JFileChooser.APPROVE_OPTION) {
					final String dir = fc.getSelectedFile().getPath();

					final Enumeration<DirItem> en = listModel.elements();
					boolean err = false;
					while (en.hasMoreElements()) {
						final String sdir = en.nextElement().getPath();
						if (dir.equals(sdir) || dir.contains(sdir) || sdir.contains(dir)) {
							JOptionPane.showMessageDialog(this.getRoot(),
									"Target dir should be different than source dirs",
									"Error",
									JOptionPane.ERROR_MESSAGE);
							err = true;
							break;
						}
					}
					if (err) {
						continue;
					}
					this.progressStateManager.startProcess();
					return;
				} else {
					return;
				}
			}
		}
	}

	private Container getRoot() {
		return this.getParent().getParent().getParent();
	}

}
