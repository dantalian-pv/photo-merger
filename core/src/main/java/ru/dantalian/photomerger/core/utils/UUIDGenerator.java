package ru.dantalian.photomerger.core.utils;

import java.util.UUID;

public class UUIDGenerator {
	
	private UUIDGenerator() {}

	public static String random(final int aSize) {
		return random().substring(0, aSize);
	}

	public static String random() {
		return UUID.randomUUID().toString().replace("-", "");
	}

}
