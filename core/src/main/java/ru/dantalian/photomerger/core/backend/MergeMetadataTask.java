package ru.dantalian.photomerger.core.backend;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.dantalian.photomerger.core.AbstractExecutionTask;
import ru.dantalian.photomerger.core.TaskExecutionException;
import ru.dantalian.photomerger.core.backend.commands.MergeMetadataCommand;
import ru.dantalian.photomerger.core.events.MergeMetadataEvent;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;

public class MergeMetadataTask extends AbstractExecutionTask<DirItem> {

	private static final Logger logger = LoggerFactory.getLogger(MergeMetadataTask.class);

	private final EventManager events;

	private final DirItem targetDir;

	private final long totalCount;

	private final AtomicLong filesCount = new AtomicLong(0);

	private final ThreadPoolExecutor pool;

	private final List<DirItem> metadataFiles;

	public MergeMetadataTask(final DirItem targetDir,
			final List<DirItem> metadataFiles, final EventManager events) {
		this(targetDir, metadataFiles, events, ThreadPoolFactory.getThreadPool(ThreadPoolFactory.MERGE_META_POOL));
	}

	public MergeMetadataTask(final DirItem targetDir, final List<DirItem> metadataFiles,
			final EventManager events, final ThreadPoolExecutor pool) {
		this.events = events;
		this.targetDir = targetDir;
		this.metadataFiles = Collections.unmodifiableList(metadataFiles);
		this.pool = pool;

		// Geometric progression with b1 = size q = 1/2 and n = log2(size)
		totalCount = (long) (metadataFiles.size() * 2 * (1 - Math.pow(0.5,
				(Math.log(metadataFiles.size()) / Math.log(2))))) + 1L;
	}

	@Override
	protected List<Future<DirItem>> execute0() throws TaskExecutionException {
		List<DirItem> metadataFiles = new LinkedList<>(this.metadataFiles);
		while (true) {
			if (metadataFiles.size() == 1) {
				final DirItem finalItem = metadataFiles.get(0);
				logger.info("Complete merged metadata {}", finalItem);
				return Collections.singletonList(CompletableFuture.completedFuture(finalItem));
			}
			final List<MergeMetadataCommand> commands = new LinkedList<>();

			final Iterator<DirItem> iterator = metadataFiles.iterator();
			boolean createEmpty = true;
			while (iterator.hasNext()) {
				if (this.interrupted.get()) {
					return Collections.singletonList(CompletableFuture.completedFuture(iterator.next()));
				}
				final DirItem left = iterator.next();
				this.events.publish(new MergeMetadataEvent(filesCount.incrementAndGet(), totalCount));
				if (!iterator.hasNext()) {
					metadataFiles = new LinkedList<>();
					metadataFiles.add(left);
					createEmpty = false;
					break;
				}
				final DirItem right = iterator.next();
				this.events.publish(new MergeMetadataEvent(filesCount.incrementAndGet(), totalCount));

				commands.add(new MergeMetadataCommand(left, right, this.targetDir));
			}

			try {
				final List<Future<DirItem>> futures = pool.invokeAll(commands);
				if (createEmpty) {
					metadataFiles = new LinkedList<>();
				}
				for (final Future<DirItem> future : futures) {
					metadataFiles.add(future.get());
				}
			} catch (final InterruptedException | ExecutionException e) {
				throw new TaskExecutionException("Failed to merge metadata", e);
			}
		}
	}

}
