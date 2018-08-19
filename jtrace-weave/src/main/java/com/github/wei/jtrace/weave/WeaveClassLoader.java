package com.github.wei.jtrace.weave;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.core.extension.ExtensionJarInfo;

public class WeaveClassLoader extends URLClassLoader{
	static Logger log = LoggerFactory.getLogger("WeaveClassLoader");
	private ClassLoader jtraceClassLoader;
	public WeaveClassLoader(ClassLoader targetClasLoader, ClassLoader jtraceClassLoader, ExtensionJarInfo jarInfo) throws MalformedURLException {
		super(new URL[] {jarInfo.getFile().toURI().toURL()}, targetClasLoader);
		this.jtraceClassLoader = jtraceClassLoader;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if(name.startsWith("com.github.wei.jtrace")) {
			try { 
				return this.jtraceClassLoader.loadClass(name);
			}catch(ClassNotFoundException e) {
				log.warn("No such class({}) was found from {}.", new Object[] {name, jtraceClassLoader});
			}
		}
		return super.loadClass(name, resolve);
	}
}
