package com.github.wei.jtrace.core.resource;

import java.lang.ref.WeakReference;
import java.net.URL;

import com.github.wei.jtrace.api.resource.IResource;

public class DefaultResource implements IResource{
	private WeakReference<ClassLoader> ref;
	private URL url;
	
	public DefaultResource(WeakReference<ClassLoader> ref, URL path) {
		this.ref = ref;
		this.url = path;
	}
	
	@Override
	public ClassLoader getClassLoader() {
		return ref.get();
	}

	@Override
	public URL getURL() {
		return url;
	}

	@Override
	public String toString() {
		return "Resource["+ref.get()+"]:" + url;
	}
}
