package com.github.wei.jtrace.asm.test;

import com.github.wei.jtrace.api.clazz.MethodDescriber;
import com.github.wei.jtrace.asm.api.matcher.IMethodMatcher;
import com.github.wei.jtrace.asm.api.matcher.MethodExtractMatcher;
import com.github.wei.jtrace.core.test.BaseTest;
import com.github.wei.jtrace.core.util.ClazzUtil;
import org.junit.Test;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.Assert.*;

public class MethodExtractMatcherTest  extends BaseTest{

	@Test
	public void testExtractMatcher() {
		IMethodMatcher methodMatcher = new MethodExtractMatcher("test", "(Ljava.lang.String;Ljava.lang.Integer;)");

		MethodNode mn = new MethodNode(0x1, "test", "(Ljava/lang/String;Ljava/lang/Integer;)V", null, null);

		assertTrue(methodMatcher.match(mn));
	}
	
	@Test
	public void testRegex() {
		IMethodMatcher methodMatcher = new MethodExtractMatcher("^test.*", "(Ljava.lang.String;Ljava.lang.Integer;)");

		MethodNode mn = new MethodNode(0x1, "testRegex", "(Ljava/lang/String;Ljava/lang/Integer;)V", null, null);

		assertTrue(methodMatcher.match(mn));
	}
	
	@Test
	public void testExtractMatcherNoReturn() {
		MethodExtractMatcher methodMatcher = new MethodExtractMatcher("test", "(Ljava.lang.String;Ljava.lang.Integer;)V");
		assertEquals("method return type not matched", "void", methodMatcher.getReturnType());
	}
	
	@Test
	public void testExtractMatcherWithArgs() {
		MethodExtractMatcher methodMatcher = new MethodExtractMatcher("test", "(Ljava.lang.String;I)V");
		assertArrayEquals(methodMatcher.getArgumentTypes(), new String[] {"java/lang/String", "int"});
	}
	
	@Test
	public void testExtractMatcherNoArgs() {
		MethodExtractMatcher methodMatcher = new MethodExtractMatcher("test", "()V");
		assertArrayEquals(methodMatcher.getArgumentTypes(), new String[0]);
	}
	
	@Test
	public void testMethodDescriber() {
		MethodDescriber methodDescriber = ClazzUtil.extractMethodDescriber(0x1, "test", "(Ljava/lang/String;Ljava/lang/Integer;)V", null);
		assertEquals("method name not matched", "test", methodDescriber.getName());
		assertEquals("method return type not matched", "void", methodDescriber.getReturnType());
		assertArrayEquals("method arguments not matched", new String[] {"java/lang/String","java/lang/Integer"}, methodDescriber.getArgumentTypes());
	}
}
