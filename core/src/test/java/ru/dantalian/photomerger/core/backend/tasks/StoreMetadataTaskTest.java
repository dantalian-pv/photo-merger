package ru.dantalian.photomerger.core.backend.tasks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import ru.dantalian.photomerger.core.backend.tasks.StoreMetadataTask;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Long.class })
public class StoreMetadataTaskTest {

	@Mock
	private EventManager events;

	@Mock
	private ThreadPoolExecutor pool;

	@Mock
	private List<DirItem> sourceDirs;

	@Mock
	private Iterator<DirItem> sourceIterator;

	@Mock
	private DirItem targetDir;

	@Mock
	private Long totalCount;

	@InjectMocks
	private StoreMetadataTask storeMetadataTask;

	@Before
	public void init() {
		Mockito.when(sourceDirs.iterator()).thenReturn(sourceIterator);
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNPE() throws Exception {
		StoreMetadataTask smt = new StoreMetadataTask(null, null, null, null);
		smt.execute();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalState() throws Exception {
		StoreMetadataTask smt = new StoreMetadataTask(new LinkedList<>(), null, null, null);
		smt.execute();
	}

	@Test
	public void shouldSubmitOneTask() throws Exception {
		List<Future<List<DirItem>>> futures = storeMetadataTask.execute();
		Mockito.verify(pool, Mockito.times(1)).submit(Mockito.any(Callable.class));
		Assert.assertThat(futures.size(), CoreMatchers.equalTo(1));
	}

	@Test
	public void shouldSubmitTwoTasks() throws Exception {
		AtomicBoolean hasNext = new AtomicBoolean(true);
		Mockito.when(sourceIterator.hasNext()).thenAnswer(new Answer<Boolean>() {

			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				return hasNext.getAndSet(false);
			}

		});

		List<Future<List<DirItem>>> futures = storeMetadataTask.execute();
		Assert.assertThat(futures.size(), CoreMatchers.equalTo(2));
		Mockito.verify(pool, Mockito.times(2)).submit(Mockito.any(Callable.class));
	}
	
	@Test
	public void shouldSubmitFourTasks() throws Exception {
		AtomicInteger sources = new AtomicInteger();
		Mockito.when(sourceIterator.hasNext()).thenAnswer(new Answer<Boolean>() {

			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				return sources.incrementAndGet() < 4;
			}

		});

		List<Future<List<DirItem>>> futures = storeMetadataTask.execute();
		Assert.assertThat(futures.size(), CoreMatchers.equalTo(4));
		Mockito.verify(pool, Mockito.times(4)).submit(Mockito.any(Callable.class));
	}


}
