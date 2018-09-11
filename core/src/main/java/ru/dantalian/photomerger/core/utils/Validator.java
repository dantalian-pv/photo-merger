package ru.dantalian.photomerger.core.utils;

import java.util.Collection;
import java.util.regex.Pattern;

public final class Validator {

	public static final Pattern NAME_FOR_NAMESPACE_REGEX = Pattern.compile("^[a-z][a-z\\-]*[a-z]+$");

	private Validator() {
	}

	public static <T> T checkNotNull(final T aReference) {
		return checkNotNull(aReference, null);
	}

	public static <T> T checkNotNull(final T aReference, final String aMessage) {
		if (aReference == null) {
			throw new NullPointerException(aMessage);
		}
		return aReference;
	}

	public static void checkCondition(final boolean aCondition, final String aMessage) {
		if (!aCondition) {
			throw new IllegalArgumentException(aMessage);
		}
	}

	public static <T extends Collection<?>> T checkEmptyCollection(final T aCollection) {
		return checkEmptyCollection(aCollection, null);
	}

	public static <T extends Collection<?>> T checkEmptyCollection(final T aCollection, final String aMessage) {
		checkNotNull(aCollection);
		if (aCollection.isEmpty()) {
			throw new IllegalArgumentException(aMessage);
		}
		return aCollection;
	}

	public static String checkNameForNamespace(final String aName) {
		checkNotNull(aName);
		if (!NAME_FOR_NAMESPACE_REGEX.matcher(aName).matches()) {
			throw new IllegalArgumentException("Namespace Name should contain only lower case characters "
					+ "and dashes");
		}
		return aName;
	}

	public static int checkVersion(final int aVersion) {
		if (aVersion < 0) {
			throw new IllegalArgumentException("Namespace Version should be >= 0");
		}
		return aVersion;
	}

	public static int checkSerializationVersion(final int aVersion) {
		if (aVersion <= 0) {
			throw new IllegalArgumentException("Namespace Version should be > 0");
		}
		return aVersion;
	}

}
