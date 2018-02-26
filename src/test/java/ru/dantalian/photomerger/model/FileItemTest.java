package ru.dantalian.photomerger.model;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;

public class FileItemTest {

	@Test
	public void testCompareTo() throws Exception {
		FileItem item1 = new FileItem("", "", -9133614936204935388L);
		FileItem item2 = new FileItem("", "", -9167693199145448307L);
		assertThat(item2.compareTo(item1), Matchers.lessThan(0));
		assertThat(item1.compareTo(item2), Matchers.greaterThan(0));
	}

}
