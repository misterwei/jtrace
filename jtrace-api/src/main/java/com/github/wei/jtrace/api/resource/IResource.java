package com.github.wei.jtrace.api.resource;

import java.net.URL;

public interface IResource {
	ClassLoader getClassLoader();
	URL  getURL();
}
