package ru.dantalian.photomerger;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

public class ListDemo extends JPanel {

	private static final String openString = "+";

	private static final String startString = "Start";

	private JList list;

	private DefaultListModel listModel;

	public ListDemo() {
		super(new BorderLayout(5, 5));

		listModel = new DefaultListModel();

		// Create the list and put it in a scroll pane.
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setVisibleRowCount(5);
		list.registerKeyboardAction(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int last = -1;
				while(list.getSelectedIndex() != -1) {
					last = list.getSelectedIndex();
					listModel.remove(last);
				}
				last = Math.min(last, listModel.getSize() - 1);
				if (last >=0 && last - 1 < listModel.getSize())
					list.setSelectedIndex(last);
				list.requestFocus();
			}
		},
			    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_FOCUSED);
		JScrollPane listScrollPane = new JScrollPane(list);

		JButton openButton = new JButton(openString);
		ActionListener hireListener = new OpenButtonListener(listModel);
		openButton.setActionCommand(openString);
		openButton.addActionListener(hireListener);
		
		JButton startButton = new JButton(startString);
		ActionListener startListener = new OpenButtonListener(listModel);
		startButton.setActionCommand(startString);
		startButton.addActionListener(startListener);
		
		JProgressBar progressBar;
		//Where the GUI is constructed:
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		//progressBar.setStringPainted(true);
		progressBar.setEnabled(false);

		// Create a panel that uses BoxLayout.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BorderLayout(5, 5));
		buttonPane.add(openButton, BorderLayout.WEST);
		buttonPane.add(progressBar, BorderLayout.CENTER);
		buttonPane.add(startButton, BorderLayout.EAST);
		//buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(listScrollPane, BorderLayout.CENTER);
		add(buttonPane, BorderLayout.PAGE_END);
	}
}