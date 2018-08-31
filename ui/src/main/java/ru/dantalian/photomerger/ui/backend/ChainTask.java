package ru.dantalian.photomerger.ui.backend;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.core.backend.CalculateFilesTask;
import ru.dantalian.photomerger.core.backend.ChainStoppedException;
import ru.dantalian.photomerger.core.backend.EventManagerFactory;
import ru.dantalian.photomerger.core.backend.MergeFilesTask;
import ru.dantalian.photomerger.core.backend.MergeMetadataTask;
import ru.dantalian.photomerger.core.backend.StoreMetadataTask;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;
import ru.dantalian.photomerger.ui.ProgressStateManager;
import ru.dantalian.photomerger.ui.events.ProgressBarEvent;

public final class ChainTask extends TimerTask {

	private static final Logger logger = LoggerFactory.getLogger(ChainTask.class);

	private final ProgressStateManager progress;

	private final DirItem targetDir;

	private final boolean copy;

	private final boolean keepPath;

	private final List<DirItem> scrDirs;

	public ChainTask(final ProgressStateManager progress, final DirItem targetDir,
			List<DirItem> scrDirs, final boolean copy, final boolean keepPath) {
		this.progress = progress;
		this.targetDir = targetDir;
		this.scrDirs = Collections.unmodifiableList(scrDirs);
		this.copy = copy;
		this.keepPath = keepPath;
	}

	@Override
	public void run() {
		long filesCount = 0;
		Exception ex = null;
		try {
			checkState();
			final EventManager events = EventManagerFactory.getInstance();
			final CalculateFilesTask calculateFilesTask = new CalculateFilesTask(
					scrDirs, targetDir, events);
			final List<Future<Long>> calculateFiles = calculateFilesTask.execute();
			long calcCount = 0;
			for (final Future<Long> future : calculateFiles) {
				checkState();
				calcCount += future.get();
			}
			checkState();
			filesCount = calcCount;
			calculateFilesTask.interrupt();

			final StoreMetadataTask storeMetadataTask = new StoreMetadataTask(scrDirs, targetDir, filesCount,
					events);
			final List<Future<List<DirItem>>> storeMetadata = storeMetadataTask.execute();
			final List<DirItem> metadataFiles = new LinkedList<>();
			for (final Future<List<DirItem>> future : storeMetadata) {
				checkState();
				metadataFiles.addAll(future.get());
			}
			checkState();
			storeMetadataTask.interrupt();

			// Merging all metadata files into one
			final MergeMetadataTask mergeTask = new MergeMetadataTask(targetDir, metadataFiles, events);
			final DirItem metadataFile = mergeTask.execute().iterator().next().get();
			mergeTask.interrupt();

			final MergeFilesTask mergeFiles = new MergeFilesTask(targetDir,
					metadataFile,
					copy,
					keepPath,
					filesCount,
					events);
			mergeFiles.execute().iterator().next().get();
			mergeFiles.interrupt();
		} catch (InterruptedException e) {
			logger.error("Failed to calculate files", e);
			ex = e;
		} catch (final ChainStoppedException e) {
			// Ignore it
			ex = e;
		} catch (final Exception e) {
			logger.error("Executin chain failed", e);
			ex = e;
		} finally {
			this.progress.stopProcess();
			if (ex == null) {
				EventManagerFactory.getInstance().publish(
						new ProgressBarEvent("Succesfully finished merging " + filesCount + " files", 100));
				logger.info("Succesfully finished merging {} files", filesCount);
			} else if (!(ex instanceof ChainStoppedException)) {
				EventManagerFactory.getInstance().publish(
						new ProgressBarEvent("Error occured. See logs.", 0));
			}
		}
	}

	private void checkState() throws ChainStoppedException {
		if (!this.progress.isStarted()) {
			throw new ChainStoppedException();
		}
	}

}
