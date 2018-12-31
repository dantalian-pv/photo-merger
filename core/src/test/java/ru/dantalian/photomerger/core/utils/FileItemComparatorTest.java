package ru.dantalian.photomerger.core.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import ru.dantalian.photomerger.core.model.DirItem;
import ru.dantalian.photomerger.core.model.FileItem;

@RunWith(MockitoJUnitRunner.class)
public class FileItemComparatorTest {

	@Mock
	private DirItem targetDir;

	@InjectMocks
	private FileItemComparator fileItemComparator;

	@Test
	public void shouldReturnZeroWhenBothIsNull() throws Exception {
		Mockito.when(targetDir.getPath()).thenReturn("/target");

		Assert.assertThat(fileItemComparator.compare(null, null), CoreMatchers.equalTo(0));
	}

	@Test
	public void shouldReturnNegativeWhenFirstIsNotNull() throws Exception {
		Mockito.when(targetDir.getPath()).thenReturn("/target");
		final FileItem item1 = Mockito.mock(FileItem.class);

		Assert.assertThat(fileItemComparator.compare(item1, null), Matchers.lessThan(0));
		Assert.assertThat(fileItemComparator.compare(null, item1), Matchers.greaterThan(0));
	}

	@Test
	public void shouldReturnFirstWithSameTargetPath() throws Exception {
		Mockito.when(targetDir.getPath()).thenReturn("/target");
		final FileItem item1 = Mockito.mock(FileItem.class);
		final FileItem item2 = Mockito.mock(FileItem.class);
		Mockito.when(item1.getRootPath()).thenReturn("/target");
		Mockito.when(item2.getRootPath()).thenReturn("/target2");

		Assert.assertThat(fileItemComparator.compare(item1, item2), Matchers.lessThan(0));
		Assert.assertThat(fileItemComparator.compare(item2, item1), Matchers.greaterThan(0));
	}

	@Test
	public void shouldReturnSecondWithSameTargetPath() throws Exception {
		Mockito.when(targetDir.getPath()).thenReturn("/target");
		final FileItem item1 = Mockito.mock(FileItem.class);
		final FileItem item2 = Mockito.mock(FileItem.class);
		Mockito.when(item1.getRootPath()).thenReturn("/target2");
		Mockito.when(item2.getRootPath()).thenReturn("/target");

		Assert.assertThat(fileItemComparator.compare(item1, item2), Matchers.greaterThan(0));
		Assert.assertThat(fileItemComparator.compare(item2, item1), Matchers.lessThan(0));
	}

	@Test
	public void shouldReturnFirstWithDeeperPathButSameRootPath() throws Exception {
		Mockito.when(targetDir.getPath()).thenReturn("/target");
		final FileItem item1 = Mockito.mock(FileItem.class);
		final FileItem item2 = Mockito.mock(FileItem.class);
		Mockito.when(item1.getRootPath()).thenReturn("/target");
		Mockito.when(item1.getPath()).thenReturn("/target/one/two/three");
		Mockito.when(item2.getRootPath()).thenReturn("/target");
		Mockito.when(item2.getPath()).thenReturn("/target/one");

		Assert.assertThat(fileItemComparator.compare(item1, item2), Matchers.lessThan(0));
		Assert.assertThat(fileItemComparator.compare(item2, item1), Matchers.greaterThan(0));
	}

	@Test
	public void shouldReturnFirstWithDeeperPathAndDifferentRootPath() throws Exception {
		Mockito.when(targetDir.getPath()).thenReturn("/target");
		final FileItem item1 = Mockito.mock(FileItem.class);
		final FileItem item2 = Mockito.mock(FileItem.class);
		Mockito.when(item1.getRootPath()).thenReturn("/target1");
		Mockito.when(item1.getPath()).thenReturn("/target1/one/two/three");
		Mockito.when(item2.getRootPath()).thenReturn("/target2");
		Mockito.when(item2.getPath()).thenReturn("/target2/one");

		Assert.assertThat(fileItemComparator.compare(item1, item2), Matchers.lessThan(0));
		Assert.assertThat(fileItemComparator.compare(item2, item1), Matchers.greaterThan(0));
	}

	@Test
	public void shouldReturnFirstWithDeeperPathInSortedCollection() throws Exception {
		Mockito.when(targetDir.getPath()).thenReturn("/target");
		final FileItem item1 = Mockito.mock(FileItem.class);
		final FileItem item2 = Mockito.mock(FileItem.class);
		Mockito.when(item1.getRootPath()).thenReturn("/target1");
		Mockito.when(item1.getPath()).thenReturn("/target1/one/two/three");
		Mockito.when(item2.getRootPath()).thenReturn("/target2");
		Mockito.when(item2.getPath()).thenReturn("/target2/one");

		List<FileItem> list = Arrays.asList(item1, item2);
		Collections.sort(list, fileItemComparator);

		Assert.assertThat(list.get(0), CoreMatchers.equalTo(item1));
		Assert.assertThat(list.get(1), CoreMatchers.equalTo(item2));

		list = Arrays.asList(item2, item1);
		Collections.sort(list, fileItemComparator);

		Assert.assertThat(list.get(0), CoreMatchers.equalTo(item1));
		Assert.assertThat(list.get(1), CoreMatchers.equalTo(item2));
	}

}
