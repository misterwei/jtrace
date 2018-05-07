package com.github.wei.jtrace.core.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;
import com.github.wei.jtrace.api.resource.IClassLoaderTree;
import com.github.wei.jtrace.api.resource.IResourceSearcher;

@Bean
public class ClassLoaderTreeCommand implements ICommand{

	@AutoRef
	private IResourceSearcher resourceSearcher;
	
	@Override
	public String name() {
		return "classLoaderTree";
	}

	@Override
	public Serializable execute(Object... args) throws Exception {
		Boolean showParent = (Boolean)args[0];
		
		List<IClassLoaderTree> classLoaderTrees = resourceSearcher.getClassLoaderTree();
		
		ArrayList<ClassLoaderTreeData> dataList = new ArrayList<ClassLoaderTreeData>();
		if(classLoaderTrees != null){
			for(IClassLoaderTree tree :classLoaderTrees){
				ClassLoader loader = tree.getClassLoader();
				ClassLoader parent = loader == null? null : loader.getParent();
				
				ClassLoaderTreeData data = null;
				if(showParent != null && showParent){
					data = new ClassLoaderTreeData(toString(loader), toString(parent));
				}else{
					data = new ClassLoaderTreeData(toString(loader));
				}
				dataList.add(data);
				
				fillTreeData(tree, data, showParent);
			}
			
		}
		
		return dataList;
	}
	
	private String toString(ClassLoader loader){
		return loader == null? null : loader.toString();
	}
	
	private void fillTreeData(IClassLoaderTree tree, ClassLoaderTreeData data, Boolean showParent){
		List<IClassLoaderTree> childs = tree.getChilds();
		if(childs != null){
			for(IClassLoaderTree child : childs){
				ClassLoader loader = child.getClassLoader();
				ClassLoader parent = loader == null? null : loader.getParent();
				
				ClassLoaderTreeData childData = null;
				if(showParent != null && showParent){
					childData = new ClassLoaderTreeData(toString(loader), toString(parent));
				}else{
					childData = new ClassLoaderTreeData(toString(loader));
				}
				data.addChild(childData);
				
				fillTreeData(child, childData, showParent);
			}
		}
	}
	
	@Override
	public String introduction() {
		return "显示ClassLoader之间的关系";
	}

	@Override
	public Argument[] args() {
		return new Argument[]{
				new Argument("showParent", "显示parent classLoader", false, Boolean.class)
		};
	}
	
	
	public static class ClassLoaderTreeData implements Serializable{
		/**
		 */
		private static final long serialVersionUID = 1L;
		private String name;
		private String parent;
		
		private List<ClassLoaderTreeData> childs = new ArrayList<ClassLoaderTreeData>();
		
		public ClassLoaderTreeData(String name, String parent) {
			this.name = name;
			this.parent = parent;
		}
		
		public ClassLoaderTreeData(String name) {
			this.name = name;
		}
		
		public String getParent() {
			return parent;
		}
		
		public String getName() {
			return name;
		}
		
		public List<ClassLoaderTreeData> getChilds() {
			return childs;
		}
		
		public void addChild(ClassLoaderTreeData data){
			this.childs.add(data);
		}
	}
}
