package com.github.wei.jtrace.api.matcher;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;

public interface ITransformer{
	
	byte[]
		    transform(  ClassLoader         loader,
		    			IClassDescriberTree descr,
		                Class<?>            classBeingRedefined,
		                ProtectionDomain    protectionDomain,
		                byte[]              classfileBuffer)
		        throws IllegalClassFormatException;
	
	boolean needRetransform(IClassDescriberTree descr);
}
