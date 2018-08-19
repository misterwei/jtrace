package com.github.wei.jtrace.transform.test;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.ClassMatchException;
import com.github.wei.jtrace.api.transform.ITransformer;

public class TransformTest implements ITransformer{
	static Logger log = LoggerFactory.getLogger("TransformTest");
	
	public boolean matchClass(IClassDescriberTree descr) throws ClassMatchException {
		if(descr.getClassDescriber().getName().equals("com.test.web.classes.ClassC")) {
			return true;
		}
		return false;
	}

	public byte[] transform(ClassLoader loader, IClassDescriberTree descr, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if(descr.getClassDescriber().getName().equals("com/test/web/classes/ClassC")) {
			log.info("******* will be transform ClassC, sleep 20000");
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.info("******* will be transform ClassC, sleep over");
		}
		return null;
	}

}
