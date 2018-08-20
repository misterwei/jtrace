package com.github.wei.jtrace.transform.test;

import java.io.PrintStream;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.commons.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.clazz.MethodDescriber;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcher;
import com.github.wei.jtrace.core.transform.AbstractMatcherAndTransformer;
import com.github.wei.jtrace.core.transform.MatchedMethod;
import com.github.wei.jtrace.core.transform.matchers.ExtractClassMatcher;
import com.github.wei.jtrace.core.transform.matchers.Matcher;
import com.github.wei.jtrace.core.transform.matchers.MethodArgumentMatcher;

public class TransformTest2 extends AbstractMatcherAndTransformer{
	static Logger log = LoggerFactory.getLogger("TransformTest");

	public TransformTest2() {
		List<IMethodMatcher> methodMatchers = new ArrayList<IMethodMatcher>();
		methodMatchers.add(new MethodArgumentMatcher("testC", 1));
		addMatcher(-1, new Matcher(new ExtractClassMatcher("com/test/web/classes/ClassC"), methodMatchers));
	}
	
	@Override
	public byte[] matchedTransform(ClassLoader loader, IClassDescriberTree descr, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer,final Set<MatchedMethod> matchedMethods)
			throws IllegalClassFormatException {
		log.info("********** start transform {}", descr);
		
		ClassReader cr = new ClassReader(classfileBuffer);
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {

			@Override
			public MethodVisitor visitMethod(int access, final String name, String desc, String signature,
					String[] exceptions) {
				
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				boolean ok = false;
				for(MatchedMethod mm : matchedMethods) {
					MethodDescriber methodDescriber = mm.getMethodDescriber();
					if(methodDescriber.getName().equals(name) && methodDescriber.getDescriptor().equals(desc)) {
						ok = true;
						break;
					}
				}
				if(!ok) {
					return mv;
				}
				
				return new AdviceAdapter(Opcodes.ASM5, new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions), access, name, desc) {
					
					@Override
					protected void onMethodExit(int opcode) {
						log.info("************ method exit {}", name);
						
				        getStatic(Type.getType(System.class), "out", Type.getType(PrintStream.class));
				        push(name + " end, this is a test");
				        invokeVirtual(Type.getType(PrintStream.class), Method.getMethod("void println(String)"));
					}

				};
			}
			
		}, ClassReader.EXPAND_FRAMES);
		
		log.info("********** transform {} finished", descr);

		return cw.toByteArray();
	}
	
	

}
