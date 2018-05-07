package com.github.wei.jtrace.core.resource;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.wei.jtrace.api.resource.IClassLoaderTree;

public class DefaultClassLoaderTree implements IClassLoaderTree {

	private WeakReference<ClassLoader> classLoaderRef;
	
	private DefaultClassLoaderTree parent;
	
	private List<IClassLoaderTree> childs = new ArrayList<IClassLoaderTree>();
	
	public DefaultClassLoaderTree(ClassLoader classLoader) {
		classLoaderRef = new WeakReference<ClassLoader>(classLoader);
	}
	
	@Override
	public ClassLoader getClassLoader() {
		return classLoaderRef.get();
	}

	@Override
	public IClassLoaderTree getParent() {
		return parent;
	}

	@Override
	public List<IClassLoaderTree> getChilds() {
		return Collections.unmodifiableList(childs);
	}

	private void setParent(DefaultClassLoaderTree classLoaderTree){
		this.parent = classLoaderTree;
	}
	
	public boolean addChild(DefaultClassLoaderTree classLoaderTree){
		ClassLoader loader = classLoaderTree.getClassLoader();
		if(loader == null || loader.getParent() == null){
			return false;
		}
		
		ClassLoader thisLoader = getClassLoader();
		if(thisLoader != null && thisLoader == loader.getParent()){
			classLoaderTree.setParent(this);
			this.childs.add(classLoaderTree);
			return true;
		}
		
		for(IClassLoaderTree tree : childs){
			DefaultClassLoaderTree child = (DefaultClassLoaderTree)tree;
			boolean result = child.addChild(classLoaderTree);
			if(result){
				return true;
			}
		}
		
		return false;
	}
	
}
