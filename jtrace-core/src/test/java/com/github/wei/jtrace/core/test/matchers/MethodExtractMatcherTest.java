package com.github.wei.jtrace.core.test.matchers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.wei.jtrace.core.clazz.MethodDescriber;
import com.github.wei.jtrace.core.matchers.MethodExtractMatcher;
import com.github.wei.jtrace.core.matchers.IMethodMatcher;
import com.github.wei.jtrace.core.test.BaseTest;

public class MethodExtractMatcherTest  extends BaseTest{

	@Test
	public void testExtractMatcher() {
		IMethodMatcher methodMatcher = new MethodExtractMatcher("test", "(Ljava.lang.String;Ljava.lang.Integer;)");
		
		MethodDescriber methodDescriber = new MethodDescriber("test", "(Ljava/lang/String;Ljava/lang/Integer;)V", 0x1);

		assertTrue(methodMatcher.match(methodDescriber));
	}
	
	@Test
	public void testRegex() {
		IMethodMatcher methodMatcher = new MethodExtractMatcher("^test.*", "(Ljava.lang.String;Ljava.lang.Integer;)");
		
		MethodDescriber methodDescriber = new MethodDescriber("testRegex", "(Ljava/lang/String;Ljava/lang/Integer;)V", 0x1);

		assertTrue(methodMatcher.match(methodDescriber));
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
		MethodDescriber methodDescriber = new MethodDescriber("test", "(Ljava/lang/String;Ljava/lang/Integer;)V", 0x1);
		assertEquals("method name not matched", "test", methodDescriber.getName());
		assertEquals("method return type not matched", "void", methodDescriber.getReturnType());
		assertArrayEquals("method arguments not matched", new String[] {"java/lang/String","java/lang/Integer"}, methodDescriber.getArgumentTypes());
	}
}
