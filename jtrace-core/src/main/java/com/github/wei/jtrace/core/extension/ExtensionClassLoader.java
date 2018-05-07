package com.github.wei.jtrace.core.extension;

import java.net.URL;
import java.net.URLClassLoader;

public class ExtensionClassLoader extends URLClassLoader {

	public ExtensionClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
		
	}
	
	public ExtensionClassLoader(ClassLoader parent) {
		super(new URL[]{}, parent);
	}

	
	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}
}
