package ru.dantalian.photomerger;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class ListDemo extends JPanel {

	private static final String openString = "Open...";

	private JList list;

	private DefaultListModel listModel;

	public ListDemo() {
		super(new BorderLayout());

		listModel = new DefaultListModel();

		// Create the list and put it in a scroll pane.
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);
		list.setVisibleRowCount(5);
		JScrollPane listScrollPane = new JScrollPane(list);

		JButton openButton = new JButton(openString);
		ActionListener hireListener = new OpenButtonListener(listModel);
		openButton.setActionCommand(openString);
		openButton.addActionListener(hireListener);

		// Create a panel that uses BoxLayout.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.add(openButton);
		buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(listScrollPane, BorderLayout.CENTER);
		add(buttonPane, BorderLayout.PAGE_END);
	}
}