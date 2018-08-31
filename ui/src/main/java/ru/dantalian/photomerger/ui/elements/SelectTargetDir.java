package ru.dantalian.photomerger.ui.elements;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.ui.backend.TaskTrigger;

public class SelectTargetDir extends JButton implements ActionListener {

	private static final long serialVersionUID = 4441106526135161132L;

	private final DefaultListModel<DirItem> listModel;

	private final TaskTrigger trigger;

	private JFileChooser fc;

	public SelectTargetDir(final String text,
			final DefaultListModel<DirItem> listModel,
			final TaskTrigger trigger) {
		super(text);
		this.listModel = listModel;
		this.trigger = trigger;

		super.setActionCommand(text);
		super.addActionListener(this);

		this.fc = new JFileChooser();
		this.fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (this.trigger.isStarted()) {
			this.trigger.startStop(false, null);
		} else {
			if (listModel.isEmpty()) {
				JOptionPane.showMessageDialog(this.getRoot(),
						"No source dir is selected. Add at least one source dir.",
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
					this.trigger.startStop(true, new DirItem(new File(dir)));
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
