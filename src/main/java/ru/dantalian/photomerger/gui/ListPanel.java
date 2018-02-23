package ru.dantalian.photomerger.gui;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ru.dantalian.photomerger.ProgressStateManager;
import ru.dantalian.photomerger.model.DirItem;

public class ListPanel extends JPanel implements ProgressStateManager {

	private static final long serialVersionUID = -2151734246788327983L;

	public static final String openString = "+";

	public static final String startString = "Start";

	public static final String stopString = "Stop";

	public static final String copyString = "Copy files";
	
	public static final String moveString = "Move files";

	public static final String keepPathString = "Keep path";

	private final JList<DirItem> list;

	private final DefaultListModel<DirItem> listModel;

	private final JButton openButton;

	private final JProgressBar progressBar;

	private final JButton startButton;

	private final JCheckBox copyCheckBox;

	private final JCheckBox keepPathCheckBox;

	private volatile boolean started;

	public ListPanel() {
		super(new BorderLayout(5, 5));

		this.listModel = new DefaultListModel<DirItem>();

		this.list = new SourceDirList(listModel);
		final JScrollPane listScrollPane = new JScrollPane(list);

		this.openButton = new SelectSourceDir(openString, listModel);

		this.progressBar = new JProgressBar(0, 100);
		this.progressBar.setValue(0);
		this.progressBar.setEnabled(false);

		this.startButton = new SelectTargetDir(startString, listModel, this);

		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BorderLayout(5, 5));
		buttonPane.add(openButton, BorderLayout.WEST);
		buttonPane.add(progressBar, BorderLayout.CENTER);
		buttonPane.add(startButton, BorderLayout.EAST);

		this.copyCheckBox = new JCheckBox(copyString);
		this.copyCheckBox.setSelected(true);
		this.copyCheckBox.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(final ChangeEvent e) {
				copyCheckBox.setText((copyCheckBox.isSelected()) ? copyString : moveString);
			}
		});

		this.keepPathCheckBox = new JCheckBox(keepPathString);
		this.keepPathCheckBox.setSelected(false);

		final JPanel checkBoxPane = new JPanel();
		checkBoxPane.setLayout(new BorderLayout(5, 5));
		checkBoxPane.add(copyCheckBox, BorderLayout.WEST);
		checkBoxPane.add(keepPathCheckBox, BorderLayout.EAST);

		add(checkBoxPane, BorderLayout.PAGE_START);
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
		this.startButton.setText((start) ? stopString : startString);
		this.list.setEnabled(!start);
		this.openButton.setEnabled(!start);
		this.progressBar.setEnabled(start);
		this.progressBar.setStringPainted(start);
		this.copyCheckBox.setEnabled(!start);
		this.keepPathCheckBox.setEnabled(!start);
	}

}