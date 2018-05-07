package com.github.wei.jtrace.api.resource;

import java.util.List;

public interface IResourceSearcher {
	/**
	 * 搜索resource，只要有搜索到就返回。
	 * @param path
	 * @return 如果没有搜索到则返回null
	 */
	IResource searchResource(String path);
	
	/**
	 * 从ClassLoaderTree中搜索，父节点搜索到，子节点就不在搜索
	 * @param path
	 * @return 如果没有搜索到返回空List
	 */
	List<IResource> searchResourceFromTree(String path);
	
	/**
	 * 从所有的ClassLoader中搜索，classLoader中没有的url为null
	 * @param path
	 * @return 如果没有搜索到返回空List
	 */
	List<IResource> searchResourceFromAll(String path);
	
	List<IClassLoaderTree> getClassLoaderTree();
	
	List<ClassLoader> getClassLoaders();
}
