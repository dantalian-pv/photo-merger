package ru.dantalian.photomerger.core.model;

import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;

import ru.dantalian.photomerger.core.model.FileItem;

public class FileItemTest {

	@Test
	public void secondShouldBeLessThanFirstOnCrc() throws Exception {
		FileItem item1 = new FileItem("", "", -9133614936204935388L, 0);
		FileItem item2 = new FileItem("", "", -9167693199145448307L, 0);
		assertThat(item2.compareTo(item1), Matchers.lessThan(0));
		assertThat(item1.compareTo(item2), Matchers.greaterThan(0));
	}
	
	@Test
	public void firstShouldBeLessThanSecondOnSize() throws Exception {
		FileItem item1 = new FileItem("", "", 0, 1);
		FileItem item2 = new FileItem("", "", 0, 2);
		assertThat(item2.compareTo(item1), Matchers.greaterThan(0));
		assertThat(item1.compareTo(item2), Matchers.lessThan(0));
	}
	
	@Test
	public void shouldBeTheSameBasedOnCrc() throws Exception {
		FileItem item1 = new FileItem("", "", -9133614936204935388L, 0);
		FileItem item2 = new FileItem("", "", -9133614936204935388L, 0);
		assertThat(item2.compareTo(item1), CoreMatchers.equalTo(0));
		assertThat(item1.compareTo(item2), CoreMatchers.equalTo(0));
	}

}
