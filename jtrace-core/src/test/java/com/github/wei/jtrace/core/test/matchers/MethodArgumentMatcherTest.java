package com.github.wei.jtrace.core.test.matchers;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.wei.jtrace.core.clazz.MethodDescriber;
import com.github.wei.jtrace.core.test.BaseTest;
import com.github.wei.jtrace.core.transform.matchers.IMethodMatcher;
import com.github.wei.jtrace.core.transform.matchers.MethodArgumentMatcher;

public class MethodArgumentMatcherTest extends BaseTest{

	@Test
	public void testArgument() {
		IMethodMatcher methodMatcher = new MethodArgumentMatcher("test", 2);
		
		MethodDescriber methodDescriber = new MethodDescriber("test", "(Ljava/lang/String;Ljava/lang/Integer;)V", 0x1);

		assertTrue(methodMatcher.match(methodDescriber));
	}
	
	@Test
	public void testRegex() {
		IMethodMatcher methodMatcher = new MethodArgumentMatcher("^test.*", 2);
		
		MethodDescriber methodDescriber = new MethodDescriber("testRegex", "(Ljava/lang/String;Ljava/lang/Integer;)V", 0x1);

		assertTrue(methodMatcher.match(methodDescriber));
	}
	
}
