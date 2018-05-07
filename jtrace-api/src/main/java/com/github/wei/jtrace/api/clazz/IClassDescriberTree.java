package com.github.wei.jtrace.api.clazz;

public interface IClassDescriberTree {
	ClassDescriber getClassDescriber();
	
	IClassDescriberTree[] getInterfaces();
	
	IClassDescriberTree getSuperClass();
}
