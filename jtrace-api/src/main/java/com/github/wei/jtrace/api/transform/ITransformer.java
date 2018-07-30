package com.github.wei.jtrace.api.transform;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;

public interface ITransformer extends IClassMatcher{
	
	byte[]
		    transform(  ClassLoader         loader,
		    			IClassDescriberTree descr,
		                Class<?>            classBeingRedefined,
		                ProtectionDomain    protectionDomain,
		                byte[]              classfileBuffer)
		        throws IllegalClassFormatException;
	
}
