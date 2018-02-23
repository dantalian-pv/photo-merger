package ru.dantalian.photomerger.gui;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import ru.dantalian.photomerger.ProgressStateManager;
import ru.dantalian.photomerger.model.DirItem;

public class ListPanel extends JPanel implements ProgressStateManager {

	private static final long serialVersionUID = -2151734246788327983L;

	public static final String openString = "+";

	public static final String startString = "Start";

	public static final String stopString = "Stop";

	private final JList<DirItem> list;

	private final DefaultListModel<DirItem> listModel;

	private final JButton openButton;

	private final JProgressBar progressBar;

	private final JButton startButton;

	private volatile boolean started;

	public ListPanel() {
		super(new BorderLayout(5, 5));

		this.listModel = new DefaultListModel<DirItem>();

		this.list = new SourceDirList(listModel);
		final JScrollPane listScrollPane = new JScrollPane(list);

		openButton = new SelectSourceDir(openString, listModel);

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setEnabled(false);

		startButton = new SelectTargetDir(startString, listModel, this);

		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BorderLayout(5, 5));
		buttonPane.add(openButton, BorderLayout.WEST);
		buttonPane.add(progressBar, BorderLayout.CENTER);
		buttonPane.add(startButton, BorderLayout.EAST);

		add(listScrollPane, BorderLayout.CENTER);
		add(buttonPane, BorderLayout.PAGE_END);
	}

	@Override
	public void startProcess() {
		startStop(true);
	}

	@Override
	public void stopProcess() {
		startStop(false);
	}

	@Override
	public boolean isStarted() {
		return this.started;
	}

	private void startStop(final boolean start) {
		this.started = start;
		startButton.setText((start) ? stopString : startString);
		list.setEnabled(!start);
		openButton.setEnabled(!start);
		progressBar.setEnabled(start);
		progressBar.setStringPainted(start);
	}

}