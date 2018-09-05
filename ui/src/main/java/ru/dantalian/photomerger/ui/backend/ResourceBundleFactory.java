package ru.dantalian.photomerger.ui.backend;

import java.util.Locale;
import java.util.ResourceBundle;

import ru.dantalian.photomerger.ui.utils.UTF8Control;

public class ResourceBundleFactory {
	
	private static final ResourceBundleFactory INSTANCE = new ResourceBundleFactory();
	
	private final ResourceBundle messages;
	
	public ResourceBundleFactory() {
		final Locale locale = Locale.getDefault();
		this.messages = ResourceBundle.getBundle("i18n/ui", locale, new UTF8Control());
	}
	
	public static ResourceBundleFactory getInstance() {
		return INSTANCE;
	}
	
	public ResourceBundle getBundle() {
		return this.messages;
	}

}
