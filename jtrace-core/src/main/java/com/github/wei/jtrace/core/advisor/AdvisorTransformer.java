package com.github.wei.jtrace.core.advisor;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.clazz.MethodDescriber;
import com.github.wei.jtrace.api.transform.matcher.MatcherContext;
import com.github.wei.jtrace.core.transform.AbstractMatcherAndTransformer;
import com.github.wei.jtrace.core.transform.MatchedMethod;

public class AdvisorTransformer extends AbstractMatcherAndTransformer implements Opcodes{
	static Logger logger = LoggerFactory.getLogger("AdvisorTransformer");
	
	@Override
	public byte[] matchedTransform(final ClassLoader loader, IClassDescriberTree descr, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer, Set<MatchedMethod> matchedMethods) throws IllegalClassFormatException {
		
		if(matchedMethods.isEmpty()) {
			return null;
		}
		
		ClassReader cr = new ClassReader(classfileBuffer);
		
		if((cr.getAccess() & ACC_INTERFACE) != 0) {
			return null;
		}
		
		String className = descr.getClassDescriber().getName();
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
			
			/**
			 * 因为ClassLoader隔离，所以在ASM所在的ClassLoader中找不到应用的类，
			 * 真正的ClassLoader应该是transform传递进来的ClassLoader
			 */
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                Class<?> c, d;
                try {
                    c = Class.forName(type1.replace('/', '.'), false, loader);
                    d = Class.forName(type2.replace('/', '.'), false, loader);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (c.isAssignableFrom(d)) {
                    return type1;
                }
                if (d.isAssignableFrom(c)) {
                    return type2;
                }
                if (c.isInterface() || d.isInterface()) {
                    return "java/lang/Object";
                } else {
                    do {
                        c = c.getSuperclass();
                    } while (!c.isAssignableFrom(d));
                    return c.getName().replace('.', '/');
                }
            }
		};
		cr.accept(getClassAdvisorWriter(className, matchedMethods, cw), ClassReader.EXPAND_FRAMES);
		
		return cw.toByteArray();
	}
	
	protected ClassVisitor getClassAdvisorWriter(String className, Set<MatchedMethod> matchedMethods, ClassWriter cw) {
		return new ClassAdvisorWriter(className, matchedMethods, cw);
	}
	
	private class ClassAdvisorWriter extends ClassVisitor implements Opcodes{
		Logger logger = LoggerFactory.getLogger("ClassAdvisorWriter");
		
		private final String CLASS_NAME;
		private final Set<MatchedMethod> matchedMethods;
		public ClassAdvisorWriter(String className, Set<MatchedMethod> matchedMethods, ClassVisitor cv) {
			super(ASM5, cv);
			this.CLASS_NAME = className;
			this.matchedMethods = matchedMethods;
		}

		@Override
		public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, String[] exceptions) {
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			
			boolean isMatched = false;
			Iterator<MatchedMethod> it = matchedMethods.iterator();
			MatchedMethod mm = null;
			while(it.hasNext()) {
				mm = it.next();
				MethodDescriber methodDescr = mm.getMethodDescriber();
				if(methodDescr.getName().equals(name) && methodDescr.getDescriptor().equals(desc)) {
					isMatched = true;
					break;
				}
			}
			
			if(!isMatched) {
				return mv;
			}
			
			logger.info("Transforming method {}.{}{} ", CLASS_NAME, name, desc);
			
			return getMethodAdvisorWriter(mv, access, name, desc, mm.getContext());
		}
		
		protected MethodVisitor getMethodAdvisorWriter(MethodVisitor mv, int access, String name, String desc, MatcherContext context) {
			return new MethodAdvisorWriter(CLASS_NAME, mv, access, name, desc, context);
		}
	}
}
