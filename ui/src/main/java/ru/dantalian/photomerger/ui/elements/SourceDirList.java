package ru.dantalian.photomerger.ui.elements;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import ru.dantalian.photomerger.core.model.DirItem;

public class SourceDirList extends JList<DirItem> implements ActionListener {

	private static final long serialVersionUID = 8834207880280785351L;

	private final DefaultListModel<DirItem> listModel;

	public SourceDirList(final DefaultListModel<DirItem> listModel) {
		super(listModel);
		this.listModel = listModel;
		super.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		super.setSelectedIndex(0);
		super.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		super.setVisibleRowCount(5);
		super.registerKeyboardAction(this,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				JComponent.WHEN_FOCUSED);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		int last = -1;
		while (this.getSelectedIndex() != -1) {
			last = this.getSelectedIndex();
			listModel.remove(last);
		}
		last = Math.min(last, listModel.getSize() - 1);
		if (last >= 0 && last - 1 < listModel.getSize()) {
			this.setSelectedIndex(last);
		}
		this.requestFocus();
	}

}
