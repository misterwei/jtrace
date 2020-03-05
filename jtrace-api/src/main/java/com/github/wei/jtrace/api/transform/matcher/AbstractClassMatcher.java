package com.github.wei.jtrace.api.transform.matcher;

import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.ClassMatchException;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;

public abstract class AbstractClassMatcher implements IClassMatcher{

	public abstract  boolean isMatchSubClass();
	
	public boolean matchClass(IClassDescriberTree descrTree) throws ClassMatchException{
		ClassDescriber classDescr = null;
		try {
			classDescr = descrTree.getClassDescriber();
			
			boolean matchResult = match(classDescr);
			if(matchResult) {
				return true;
			}
			
			if(isMatchSubClass()) {
				boolean r = matchParents(descrTree);
				if(r) {
					return true;
				}
			}
		}catch(Exception e) {
			throw new ClassMatchException("error in match class " + classDescr , e);
		}
		
		return false;
	}
	
	private boolean matchParents(IClassDescriberTree descrTree) throws Exception{
		//superClass 和 interface 一起适配，后面考虑优化。
		int count = 0;
		IClassDescriberTree[] interfaces = descrTree.getInterfaces();
		if(interfaces != null) {
			count = interfaces.length;
		}
		IClassDescriberTree superClass = descrTree.getSuperClass();
		if(superClass != null) {
			count += 1;
		}

		if(count == 0){
			return false;
		}

		IClassDescriberTree[] parents = new IClassDescriberTree[count];
		int pos = 0;
		if(interfaces != null && interfaces.length > 0) {
			System.arraycopy(interfaces, 0, parents, pos, interfaces.length);
			pos = interfaces.length;
		}
		if(superClass != null) {
			parents[pos] = superClass;
		}

		for(IClassDescriberTree parentTree : parents) {
			ClassDescriber descr = parentTree.getClassDescriber();

			boolean result = match(descr);
			//log.debug("match class {} result:{}", descr, result);
			if(result) {
				return true;
			}else {
				boolean r = matchParents(parentTree);
				if(r) {
					return true;
				}
			}
		}

		return false;
	}
	
	protected abstract boolean match(ClassDescriber classDescr);
}
