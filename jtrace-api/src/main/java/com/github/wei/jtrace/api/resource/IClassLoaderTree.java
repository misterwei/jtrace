package com.github.wei.jtrace.api.resource;

import java.util.List;

public interface IClassLoaderTree {

	ClassLoader getClassLoader();
	
	IClassLoaderTree getParent();
	
	List<IClassLoaderTree> getChilds();
}
