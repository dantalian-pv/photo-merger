package ru.dantalian.photomerger.ui.elements;

import java.awt.BorderLayout;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.core.ProgressStateManager;
import ru.dantalian.photomerger.core.backend.CalculateFilesTask;
import ru.dantalian.photomerger.core.backend.ChainStoppedException;
import ru.dantalian.photomerger.core.backend.MergeFilesTask;
import ru.dantalian.photomerger.core.backend.MergeMetadataTask;
import ru.dantalian.photomerger.core.backend.StoreMetadataTask;
import ru.dantalian.photomerger.core.model.DirItem;

public class ListPanel extends JPanel implements ProgressStateManager {

	private static final Logger logger = LoggerFactory.getLogger(ListPanel.class);

	private static final long serialVersionUID = -2151734246788327983L;

	private final JList<DirItem> list;

	private final DefaultListModel<DirItem> listModel;

	private final JButton openButton;

	private final JProgressBar progressBar;

	private final JButton startButton;

	private final JCheckBox copyCheckBox;

	private final JCheckBox keepPathCheckBox;
	
	private final Timer timer = new Timer("refresh-progress", true);
	
	private final Timer chainTimer = new Timer("tasks-chain", true);
	
	private volatile boolean started;

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
		//this.progressBar.setEnabled(false);

		this.startButton = new SelectTargetDir(InterfaceStrings.START, listModel, this);

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
		
		this.timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				if (started) {
					String str = "";
					if (!progressText.isEmpty()) {
						str += progressText;
					}
					if (!progressCur.isEmpty()) {
						str += ": for " + progressCur;
					}
					if (!progressMax.isEmpty()) {
						str += " of " + progressMax;
					}
					progressBar.setString(str);
				}
			}
		}, 1000, 1000);
		
	}
	
	private DirItem targetDir;

	@Override
	public void startProcess(DirItem targetDir) {
		this.targetDir = targetDir;
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
	
	private String progressText = "";
	private String progressCur = "";
	private String progressMax = "";

	@Override
	public void setProgressText(String aText) {
		this.progressText = aText;
	}

	@Override
	public void setCurrent(String aCurrent, int aPercent) {
		this.progressCur = aCurrent;
		this.progressBar.setIndeterminate(false);
		this.progressBar.setValue(aPercent);
	}

	@Override
	public void setMax(final String aMax) {
		this.progressMax = aMax;
	}

	private void startStop(final boolean start) {
		if (start) {
			// Start long run calculations
			this.progressBar.setIndeterminate(true);
			this.setProgressText(InterfaceStrings.CALCULATING_AMOUNT_OF_WORK);
			this.chainTimer.schedule(new ChainTask(), 1);
		} else {
			// Stop all background threads
			// Reset progress bar
			progressText = "";
			progressCur = "";
			progressMax = "";
			this.progressBar.setIndeterminate(false);
			this.progressBar.setValue(0);
			this.progressBar.setString("Process aborted. Run again");
		}
		this.started = start;
		this.startButton.setText((start) ? InterfaceStrings.STOP : InterfaceStrings.START);
		this.list.setEnabled(!start);
		this.openButton.setEnabled(!start);
		//this.progressBar.setEnabled(start);
		//this.progressBar.setStringPainted(start);
		this.copyCheckBox.setEnabled(!start);
		this.keepPathCheckBox.setEnabled(!start);
	}
	
	class ChainTask extends TimerTask {

		@Override
		public void run() {
			long filesCount = 0;
			Exception ex = null;
			try {
				final List<DirItem> sourceDirs = new LinkedList<>();
				final Enumeration<DirItem> elements = listModel.elements();
				while(elements.hasMoreElements()) {
					sourceDirs.add(elements.nextElement());
				}

				checkState();
				final CalculateFilesTask calculateFilesTask = new CalculateFilesTask(ListPanel.this);
				final List<Future<Boolean>> calculateFiles = calculateFilesTask.calculateFiles(sourceDirs, targetDir);
				for (final Future<Boolean> future: calculateFiles) {
					checkState();
					future.get();
				}
				checkState();
				filesCount = calculateFilesTask.getFilesCount();
				calculateFilesTask.finish();

				final StoreMetadataTask storeMetadataTask = new StoreMetadataTask(ListPanel.this, filesCount);
				final List<Future<List<DirItem>>> storeMetadata = storeMetadataTask.storeMetadata(sourceDirs, targetDir);
				final List<DirItem> metadataFiles = new LinkedList<>();
				for (final Future<List<DirItem>> future: storeMetadata) {
					checkState();
					metadataFiles.addAll(future.get());
				}
				checkState();
				storeMetadataTask.finish();

				// Merging all metadata files into one
				MergeMetadataTask mergeTask = new MergeMetadataTask(ListPanel.this, targetDir);
				final DirItem metadataFile = mergeTask.mergeMetadata(metadataFiles);
				mergeTask.finish();
				
				MergeFilesTask mergeFiles = new MergeFilesTask(ListPanel.this,
						targetDir,
						ListPanel.this.copyCheckBox.isSelected(),
						ListPanel.this.keepPathCheckBox.isSelected(),
						filesCount);
				mergeFiles.mergeFiles(metadataFile);
				mergeFiles.finish();
			} catch (InterruptedException e) {
				logger.error("Failed to calculate files", e);
				ex = e;
			} catch(final ChainStoppedException e) {
				// Ignore it
				ex = e;
			} catch(final Exception e) {
				logger.error("Executin chain failed", e);
				ex = e;
			} finally {
				stopProcess();
				if (ex == null) {
					progressBar.setString("Succesfully interrupted merging " + filesCount + " files");
					progressBar.setValue(100);
					logger.info("Succesfully interrupted merging {} files", filesCount);
				} else if (!(ex instanceof ChainStoppedException)) {
					progressBar.setString("Error occured. See logs.");
					progressBar.setValue(0);
				}
			}
		}
		
		private void checkState() throws ChainStoppedException {
			if (!isStarted()) {
				throw new ChainStoppedException();
			}
		}
		
	}

}