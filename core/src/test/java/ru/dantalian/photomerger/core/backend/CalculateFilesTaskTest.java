package ru.dantalian.photomerger.core.backend;

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
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;

@RunWith(MockitoJUnitRunner.class)
public class CalculateFilesTaskTest {

	@Mock
	private EventManager events;

	@Mock
	private ThreadPoolExecutor pool;

	@Mock
	private List<DirItem> sourceDirs;

	@Mock
	private DirItem targetDir;

	@Mock
	private Iterator<DirItem> sourceIterator;

	@InjectMocks
	private CalculateFilesTask calculateFilesTask;

	@Before
	public void init() {
		Mockito.when(sourceDirs.iterator()).thenReturn(sourceIterator);
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNPE() throws Exception {
		CalculateFilesTask cft = new CalculateFilesTask(null, null, null);
		cft.execute();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalState() throws Exception {
		CalculateFilesTask cft = new CalculateFilesTask(new LinkedList<>(), null, null);
		cft.execute();
	}

	@Test
	public void shouldSubmitOneTask() throws Exception {
		List<Future<Long>> futures = calculateFilesTask.execute();
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

		List<Future<Long>> futures = calculateFilesTask.execute();
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

		List<Future<Long>> futures = calculateFilesTask.execute();
		Assert.assertThat(futures.size(), CoreMatchers.equalTo(4));
		Mockito.verify(pool, Mockito.times(4)).submit(Mockito.any(Callable.class));
	}

}
