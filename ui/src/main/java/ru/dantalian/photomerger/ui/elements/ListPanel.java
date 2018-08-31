package ru.dantalian.photomerger.ui.elements;

import java.awt.BorderLayout;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ru.dantalian.photomerger.core.backend.EventManagerFactory;
import ru.dantalian.photomerger.core.events.CalculateFilesEvent;
import ru.dantalian.photomerger.core.events.MergeFilesEvent;
import ru.dantalian.photomerger.core.events.MergeMetadataEvent;
import ru.dantalian.photomerger.core.events.StoreMetadataEvent;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventListener;
import ru.dantalian.photomerger.core.model.EventManager;
import ru.dantalian.photomerger.ui.backend.ChainTask;
import ru.dantalian.photomerger.ui.backend.OffsetProgressCalculator;
import ru.dantalian.photomerger.ui.backend.ProgressBarTask;
import ru.dantalian.photomerger.ui.backend.TaskTrigger;
import ru.dantalian.photomerger.ui.events.CalculateFilesListener;
import ru.dantalian.photomerger.ui.events.MergeFilesListener;
import ru.dantalian.photomerger.ui.events.MergeMetadataListener;
import ru.dantalian.photomerger.ui.events.ProgressBarEvent;
import ru.dantalian.photomerger.ui.events.ProgressBarEvent.ProgressBarMessage;
import ru.dantalian.photomerger.ui.events.StoreMetadataListener;

public class ListPanel extends JPanel implements TaskTrigger {

	private static final long serialVersionUID = 5256789631660062837L;

	private final JList<DirItem> list;

	private final DefaultListModel<DirItem> listModel;

	private final JButton openButton;

	private final JProgressBar progressBar;

	private final JButton startButton;

	private final JCheckBox copyCheckBox;

	private final JCheckBox keepPathCheckBox;

	private final ProgressBarTask progressTask;

	private final Timer timer = new Timer("refresh-progress", true);

	private final Timer chainTimer = new Timer("tasks-chain", true);

	public ListPanel() {
		super(new BorderLayout(5, 5));

		this.listModel = new DefaultListModel<DirItem>();

		this.list = new SourceDirList(listModel);
		final JScrollPane listScrollPane = new JScrollPane(list);

		this.openButton = new SelectSourceDir(InterfaceStrings.ADD, listModel);

		this.progressBar = new JProgressBar(0, 100);
		this.progressBar.setValue(0);
		this.progressBar.setString("<- Add source directories and then select a target ->");
		this.progressBar.setStringPainted(true);

		this.progressTask = new ProgressBarTask(this, this.progressBar);
		this.timer.scheduleAtFixedRate(this.progressTask, 1000, 1000);

		this.startButton = new SelectTargetDir(InterfaceStrings.START, listModel, this.progressTask);

		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BorderLayout(5, 5));
		buttonPane.add(openButton, BorderLayout.WEST);
		buttonPane.add(progressBar, BorderLayout.CENTER);
		buttonPane.add(startButton, BorderLayout.EAST);

		this.copyCheckBox = new JCheckBox(InterfaceStrings.COPY);
		this.copyCheckBox.setSelected(true);
		this.copyCheckBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(final ChangeEvent e) {
				copyCheckBox.setText((copyCheckBox.isSelected()) ? InterfaceStrings.COPY : InterfaceStrings.MOVE);
			}
		});

		this.keepPathCheckBox = new JCheckBox(InterfaceStrings.KEEP_PATH);
		this.keepPathCheckBox.setSelected(true);

		final JPanel checkBoxPane = new JPanel();
		checkBoxPane.setLayout(new BorderLayout(5, 5));
		checkBoxPane.add(copyCheckBox, BorderLayout.WEST);
		checkBoxPane.add(keepPathCheckBox, BorderLayout.EAST);

		add(checkBoxPane, BorderLayout.PAGE_START);
		add(listScrollPane, BorderLayout.CENTER);
		add(buttonPane, BorderLayout.PAGE_END);

		initListeners();
	}

	@Override
	public void startStop(final boolean start, final DirItem targetDir) {
		if (start) {
			// Start long run calculations
			this.progressTask.setCurrent("", -1);
			this.progressTask.setProgressText(InterfaceStrings.CALCULATING_AMOUNT_OF_WORK);
			runChain(targetDir);
		} else {
			// Stop all background threads
			// Reset progress bar
			this.progressTask.setProgressText("Process aborted. Run again");
			this.progressTask.setCurrent("", 0);
			this.progressTask.setMax("");
		}
		this.progressTask.setStarted(start);
		this.startButton.setText((start) ? InterfaceStrings.STOP : InterfaceStrings.START);
		this.list.setEnabled(!start);
		this.openButton.setEnabled(!start);
		this.copyCheckBox.setEnabled(!start);
		this.keepPathCheckBox.setEnabled(!start);
	}

	private void runChain(final DirItem targetDir) {
		final Enumeration<DirItem> elements = this.listModel.elements();
		final List<DirItem> scrDirs = new LinkedList<>();
		while (elements.hasMoreElements()) {
			scrDirs.add(elements.nextElement());
		}
		this.chainTimer.schedule(new ChainTask(
				this.progressTask,
				targetDir,
				scrDirs,
				this.copyCheckBox.isSelected(),
				this.keepPathCheckBox.isSelected()), 1);
	}

	private void initListeners() {
		final EventManager events = EventManagerFactory.getInstance();
		events.subscribe(CalculateFilesEvent.TOPIC, new CalculateFilesListener(this.progressTask));
		events.subscribe(StoreMetadataEvent.TOPIC, new StoreMetadataListener(this.progressTask,
				new OffsetProgressCalculator(0)));
		events.subscribe(MergeMetadataEvent.TOPIC, new MergeMetadataListener(this.progressTask,
				new OffsetProgressCalculator(33)));
		events.subscribe(MergeFilesEvent.TOPIC, new MergeFilesListener(this.progressTask,
				new OffsetProgressCalculator(66)));
		events.subscribe(ProgressBarEvent.TOPIC, new EventListener<ProgressBarEvent>() {

			@Override
			public void handle(final ProgressBarEvent event) {
				final ProgressBarMessage message = event.getItem();
				progressBar.setIndeterminate(message.getValue() < 0);
				progressBar.setString(message.getMessage());
				progressBar.setValue(message.getValue());
			}

		});
	}

}
