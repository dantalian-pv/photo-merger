package ru.dantalian.photomerger.core.backend.commands;

import java.io.File;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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

import ru.dantalian.photomerger.core.backend.FileTreeWalker;
import ru.dantalian.photomerger.core.backend.commands.CalculateFilesCommand;
import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.EventManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AtomicBoolean.class })
public class CalculateFilesCommandTest {

	@Mock
	private DirItem dirItem;

	@Mock
	private EventManager events;

	@Mock
	private FileTreeWalker fileTreeWalker;

	@Mock
	private AtomicLong filesCount;

	@Mock
	private AtomicBoolean interrupted;

	@InjectMocks
	private CalculateFilesCommand calculateFilesCall;

	@Before
	public void init() {
		Mockito.when(interrupted.get()).thenReturn(Boolean.FALSE);

		File dirFile = Mockito.mock(File.class);
		Path dirPath = Mockito.mock(Path.class);
		Mockito.when(dirFile.toPath()).thenReturn(dirPath);
		Mockito.when(dirPath.toFile()).thenReturn(dirFile);
		Mockito.when(dirItem.getDir()).thenReturn(dirFile);
	}

	@Test
	public void shouldCountZeroItems() throws Exception {
		BasicFileAttributes attrs = Mockito.mock(BasicFileAttributes.class);
		Mockito.when(attrs.isRegularFile()).thenReturn(Boolean.FALSE);
		File file = Mockito.mock(File.class);
		Path filePath = Mockito.mock(Path.class);
		Mockito.when(filePath.toFile()).thenReturn(file);

		Mockito.when(fileTreeWalker.walkFileTree(Mockito.any(), Mockito.anySet(), Mockito.anyInt(), Mockito.any()))
				.thenAnswer(new Answer<Path>() {

					@Override
					public Path answer(final InvocationOnMock invocation) throws Throwable {
						((FileVisitor<Path>) invocation.getArguments()[3]).visitFile(filePath, attrs);
						return dirItem.getDir().toPath();
					}

				});

		Long result = calculateFilesCall.call();
		Assert.assertThat(result, CoreMatchers.equalTo(0L));
		Mockito.verify(events, Mockito.times(0)).publish(Mockito.any());
	}

	@Test
	public void shouldCountOneItem() throws Exception {
		BasicFileAttributes attrs = Mockito.mock(BasicFileAttributes.class);
		Mockito.when(attrs.isRegularFile()).thenReturn(Boolean.TRUE);
		File file = Mockito.mock(File.class);
		Path filePath = Mockito.mock(Path.class);
		Mockito.when(filePath.toFile()).thenReturn(file);

		Mockito.when(fileTreeWalker.walkFileTree(Mockito.any(), Mockito.anySet(), Mockito.anyInt(), Mockito.any()))
				.thenAnswer(new Answer<Path>() {

					@Override
					public Path answer(final InvocationOnMock invocation) throws Throwable {
						((FileVisitor<Path>) invocation.getArguments()[3]).visitFile(filePath, attrs);
						return dirItem.getDir().toPath();
					}

				});

		Long result = calculateFilesCall.call();
		Assert.assertThat(result, CoreMatchers.equalTo(1L));
		Mockito.verify(events, Mockito.times(1)).publish(Mockito.any());
	}
	
	@Test
	public void shouldCountThreeItem() throws Exception {
		BasicFileAttributes attrs = Mockito.mock(BasicFileAttributes.class);
		Mockito.when(attrs.isRegularFile()).thenReturn(Boolean.TRUE);
		File file = Mockito.mock(File.class);
		Path filePath = Mockito.mock(Path.class);
		Mockito.when(filePath.toFile()).thenReturn(file);

		Mockito.when(fileTreeWalker.walkFileTree(Mockito.any(), Mockito.anySet(), Mockito.anyInt(), Mockito.any()))
				.thenAnswer(new Answer<Path>() {

					@Override
					public Path answer(final InvocationOnMock invocation) throws Throwable {
						((FileVisitor<Path>) invocation.getArguments()[3]).visitFile(filePath, attrs);
						((FileVisitor<Path>) invocation.getArguments()[3]).visitFile(filePath, attrs);
						((FileVisitor<Path>) invocation.getArguments()[3]).visitFile(filePath, attrs);
						((FileVisitor<Path>) invocation.getArguments()[3]).preVisitDirectory(filePath, attrs);
						return dirItem.getDir().toPath();
					}

				});

		Long result = calculateFilesCall.call();
		Assert.assertThat(result, CoreMatchers.equalTo(3L));
		Mockito.verify(events, Mockito.times(3)).publish(Mockito.any());
	}

}
