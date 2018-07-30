package com.github.wei.jtrace.core.transform.matchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.ClassMatchException;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;

public abstract class AbstractClassMatcher implements IClassMatcher{
	
	private List<String> matchedClasses = new CopyOnWriteArrayList<String>();
	private List<String> noMatchedClasses = new CopyOnWriteArrayList<String>();
	
	public abstract  boolean isMatchSubClass();
	
	private Boolean innerMatchResult(String className){
		if(matchedClasses.contains(className)) {
			return true;
		}
		if(noMatchedClasses.contains(className)) {
			return false;
		}
		return null;
	}
	
	private synchronized void storeMatchedClasses(List<String> classes) {
		for(String clazz : classes) {
			storeMatchedClass(clazz);
		}
	}
	
	private synchronized void storeMatchedClass(String clazz) {
		int index = noMatchedClasses.indexOf(clazz);
		if(index > -1) {
			noMatchedClasses.remove(index);
		}
		
		if(matchedClasses.contains(clazz)) {
			return;
		}
		matchedClasses.add(clazz);
	}
	
	private synchronized void storeNoMatchedClasses(List<String> classes) {
		for(String clazz : classes) {
			storeNoMatchedClass(clazz);
		}
	}
	
	private synchronized void storeNoMatchedClass(String clazz) {
		if(matchedClasses.contains(clazz)) {
			return;
		}
		if(noMatchedClasses.contains(clazz)) {
			return;
		}
		noMatchedClasses.add(clazz);
	}
	
	public boolean matchClass(IClassDescriberTree descrTree) throws ClassMatchException{
		ClassDescriber classDescr = null;
		try {
			classDescr = descrTree.getClassDescriber();
			Boolean result = innerMatchResult(classDescr.getName());
			if(result != null) {
				return result;
			}
			
			boolean matchResult = match(classDescr);
			if(matchResult) {
				storeMatchedClass(classDescr.getName());
				return true;
			}
			
			if(isMatchSubClass()) {
				List<String> classes = new ArrayList<String>();
				classes.add(classDescr.getName());
				boolean r = matchParents(descrTree, classes);
				if(r) {
					return true;
				}
			}
		}catch(Exception e) {
			throw new ClassMatchException("error in match class " + classDescr , e);
		}
		
		storeNoMatchedClass(classDescr.getName());
		return false;
	}
	
	private boolean matchParents(IClassDescriberTree descrTree, List<String> classes) throws Exception{
		//superClass 和 interface 一起适配，后面考虑优化。
		IClassDescriberTree[] parents = null;
		IClassDescriberTree[] interfaces = descrTree.getInterfaces();
		if(interfaces != null) {
			parents = interfaces;
		}
		
		IClassDescriberTree superClass = descrTree.getSuperClass();
		if(superClass != null) {
			if(parents != null) {
				int lastIndex = parents.length;
				parents = Arrays.copyOf(parents, lastIndex + 1);
				parents[lastIndex] = superClass;
			}else {
				parents = new IClassDescriberTree[] {superClass};
			}
		}
		
		List<String> localNoMatchedClasses = null;
		if(parents != null && parents.length > 0) {
			localNoMatchedClasses = new ArrayList<String>(parents.length);
			
			for(IClassDescriberTree parentTree : parents) {
				ClassDescriber descr = parentTree.getClassDescriber();
				Boolean checkResult = innerMatchResult(descr.getName());
				if(checkResult != null) {
					if(checkResult.booleanValue()) {
						if(localNoMatchedClasses.size() > 0) {
							storeNoMatchedClasses(localNoMatchedClasses);
						}
						storeMatchedClasses(classes);
						return true;
					}else {
						continue;
					}
				}
				
				boolean result = match(descr);
				//log.debug("match class {} result:{}", descr, result);
				if(result) {
					if(localNoMatchedClasses.size() > 0) {
						storeNoMatchedClasses(localNoMatchedClasses);
					}
					classes.add(descr.getName());
					storeMatchedClasses(classes);
					return true;
				}else {
					classes = new ArrayList<String>(classes);
					classes.add(descr.getName());
					boolean r = matchParents(parentTree, classes);
					if(r) {
						if(localNoMatchedClasses.size() > 0) {
							storeNoMatchedClasses(localNoMatchedClasses);
						}
						return true;
					}
					localNoMatchedClasses.add(descr.getName());
				}
			}
			
		}
		
		if(localNoMatchedClasses != null) {
			storeNoMatchedClasses(localNoMatchedClasses);
		}
		return false;
	}
	
	protected abstract boolean match(ClassDescriber classDescr);
}
