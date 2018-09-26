package ru.dantalian.photomerger.ui.elements;

import java.io.File;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.Timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import ru.dantalian.photomerger.ui.backend.ResourceBundleFactory;
import ru.dantalian.photomerger.ui.backend.TaskTrigger;
import ru.dantalian.photomerger.ui.events.CalculateFilesListener;
import ru.dantalian.photomerger.ui.events.MergeFilesListener;
import ru.dantalian.photomerger.ui.events.MergeMetadataListener;
import ru.dantalian.photomerger.ui.events.ProgressBarEvent;
import ru.dantalian.photomerger.ui.events.ProgressBarEvent.ProgressBarMessage;
import ru.dantalian.photomerger.ui.events.StoreMetadataListener;

public class MainFrameLayout implements TaskTrigger {

	private static final Logger logger = LoggerFactory.getLogger(MainFrameLayout.class);

	private final ResourceBundle messages;

	private final ProgressBarTask progressTask;

	private final List list;

	private final Button copyButton;

	private final Button keepButton;
	
	private final Button srcButton;

	private final Button targetButton;

	private TextProgressBar progressBar;

	private final Timer timer = new Timer("refresh-progress", true);

	private final Timer chainTimer = new Timer("tasks-chain", true);

	private volatile ChainTask currentChain;

	public MainFrameLayout(final Shell shell) {
		messages = ResourceBundleFactory.getInstance().getBundle();

		Composite top = new Composite(shell, SWT.NONE);
		GridLayout d1 = new GridLayout(3, false);
		top.setLayout(d1);

		this.copyButton = new Button(top, SWT.CHECK);
		this.copyButton.setText(messages.getString(InterfaceStrings.COPY));
		this.copyButton.setSelection(true);
		this.copyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				copyButton.setText((copyButton.getSelection())
						? messages.getString(InterfaceStrings.COPY) : messages.getString(InterfaceStrings.MOVE));
				top.layout();
			}
		});

		final Label separator = new Label(top, SWT.HORIZONTAL);
    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
    this.keepButton = new Button(top, SWT.CHECK);
    this.keepButton.setText(messages.getString(InterfaceStrings.KEEP_PATH));
    this.keepButton.setSelection(true);

		GridData d2 = new GridData(SWT.FILL, SWT.TOP, true, false);
		top.setLayoutData(d2);
		
		this.list = new List(shell, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		this.list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.list.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					final int[] selectionIndices = list.getSelectionIndices();
					list.remove(selectionIndices);
				}
			}
		});
		
		Composite bottom = new Composite(shell, SWT.NONE);
		GridLayout d3 = new GridLayout(3, false);
		bottom.setLayout(d3);
		
		srcButton = new Button(bottom, SWT.PUSH);
		srcButton.setText(messages.getString(InterfaceStrings.ADD));
		srcButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final DirectoryDialog dd = new DirectoryDialog(shell, SWT.APPLICATION_MODAL);
				dd.setMessage(messages.getString(InterfaceStrings.ADD_SOURCE));
				dd.setText(messages.getString(InterfaceStrings.SET_DIR));
				final String string = dd.open();
				if (string != null) {
					final String s = string + "/";
					final String[] items = list.getItems();
					boolean found = false;
					for (String sdir: items) {
						sdir += "/";
						if (s.contains(sdir) || sdir.contains(s)) {
							found = true;
							break;
						}
					}
					if (found) {
						boolean parent = false;
						for (String sdir: items) {
							sdir += "/";
							if (sdir.contains(s) && !sdir.equals(s)) {
								parent = true;
							}
						}
						if (parent) {
							final MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
			        
			        messageBox.setText(messages.getString(InterfaceStrings.PARENT_DIR));
			        messageBox.setMessage(messages.getString(InterfaceStrings.REPLACE_WITH_PARENT));
			        final int buttonID = messageBox.open();
		          if (buttonID == SWT.YES) {
		            // Remove all children directories and then add parent
		          	for (int i = items.length - 1; i >= 0; i--) {
		          		final String sdir = items[i] + "/";
		          		if (sdir.contains(s)) {
		          			list.remove(i);
		          		}
		          	}
		          	logger.info("add {}", string);
								list.add(string);
		          }
						}
					} else {
						logger.info("add {}", string);
						list.add(string);
					}
				}
			}
		});
		
		progressBar = new TextProgressBar(bottom, SWT.SMOOTH);
		progressBar.setLayoutData(new GridData(GridData.FILL_BOTH));
		progressBar.setText(messages.getString(InterfaceStrings.ADD_SOURCE_SET_TARGET));
		progressBar.setShowText(true);
		
		this.targetButton = new Button(bottom, SWT.PUSH);
		this.targetButton.setText(messages.getString(InterfaceStrings.START));
		this.targetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (isStarted()) {
					startStop(false, null);
				} else {
					if (list.getItemCount() == 0) {
						final MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		        messageBox.setText(messages.getString(InterfaceStrings.ERROR_TITLE));
		        messageBox.setMessage(messages.getString(InterfaceStrings.NO_SOURCE));
		        messageBox.open();
						return;
					}
					while (true) {
						final DirectoryDialog dd = new DirectoryDialog(shell, SWT.APPLICATION_MODAL);
						dd.setMessage(messages.getString(InterfaceStrings.SET_TARGET));
						dd.setText(messages.getString(InterfaceStrings.SET_DIR));
						final String dir = dd.open();
						if (dir != null) {
							final String tdir = dir + "/";
							final String[] items = list.getItems();
							boolean err = false;
							for (String sdir: items) {
								sdir += "/";
								if (tdir.equals(sdir) || tdir.contains(sdir) || sdir.contains(tdir)) {
									final MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					        messageBox.setText(messages.getString(InterfaceStrings.ERROR_TITLE));
					        messageBox.setMessage(messages.getString(InterfaceStrings.DIFFER_TARGET));
					        messageBox.open();
									err = true;
									break;
								}
							}
							if (err) {
								continue;
							}
							startStop(true, new DirItem(new File(dir)));
							return;
						} else {
							return;
						}
					}
				}
			}
		});

		GridData d4 = new GridData(SWT.FILL, SWT.TOP, true, false);
		bottom.setLayoutData(d4);

		this.progressTask = new ProgressBarTask(EventManagerFactory.getInstance());
		this.timer.scheduleAtFixedRate(this.progressTask, 0, 1000);
		this.progressTask.setProgress(progressBar.getText(), 0);

		initListeners();
	}
	
	@Override
	public void startStop(final boolean start, final DirItem targetDir) {
		if (start) {
			// Start long run calculations
			this.progressTask.startProcess();
			this.progressTask.setProgress(messages.getString(
					InterfaceStrings.CALCULATING_AMOUNT_OF_WORK), -1);
			runChain(targetDir);
		} else {
			// Stop all background threads
			// Reset progress bar
			this.currentChain.interrupt();
			this.progressTask.stopProcess(messages.getString(InterfaceStrings.ABORTING), -1);
		}
		changeState(start, start);
	}

	@Override
	public boolean isStarted() {
		return this.progressTask.isStarted();
	}

	private void runChain(final DirItem targetDir) {
		final java.util.List<DirItem> scrDirs = new LinkedList<>();
		for (final String item: this.list.getItems()) {
			scrDirs.add(new DirItem(new File(item)));
		}
		this.currentChain = new ChainTask(
				this.progressTask,
				targetDir,
				scrDirs,
				this.copyButton.getSelection(),
				this.keepButton.getSelection(),
				this.messages);
		this.chainTimer.schedule(this.currentChain, 1);
	}

	private void changeState(final boolean start, final boolean enableStart) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				targetButton.setEnabled(enableStart);
				targetButton.setText((start)
						? messages.getString(InterfaceStrings.STOP) : messages.getString(InterfaceStrings.START));
				list.setEnabled(!start);
				srcButton.setEnabled(!start);
				copyButton.setEnabled(!start);
				keepButton.setEnabled(!start);
			}
			
		});
	}

	private void initListeners() {
		final EventManager events = EventManagerFactory.getInstance();
		events.subscribe(CalculateFilesEvent.TOPIC, new CalculateFilesListener(this.progressTask, this.messages));
		events.subscribe(StoreMetadataEvent.TOPIC, new StoreMetadataListener(this.progressTask,
				new OffsetProgressCalculator(0), this.messages));
		events.subscribe(MergeMetadataEvent.TOPIC, new MergeMetadataListener(this.progressTask,
				new OffsetProgressCalculator(33), this.messages));
		events.subscribe(MergeFilesEvent.TOPIC, new MergeFilesListener(this.progressTask,
				new OffsetProgressCalculator(66), this.messages));
		events.subscribe(ProgressBarEvent.TOPIC, new EventListener<ProgressBarEvent>() {

			@Override
			public void handle(final ProgressBarEvent event) {
				final ProgressBarMessage message = event.getItem();
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						//progressBar.getProgressBar().setIndeterminate(message.getValue() < 0);
						progressBar.setText(message.getMessage());
						progressBar.setSelection(message.getValue());
						if (!progressTask.isStarted()) {
							changeState(false, true);
							currentChain = null;
						}
					}
					
				});
			}

		});
	}

}
