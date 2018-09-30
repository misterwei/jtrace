package com.github.wei.jtrace.core.test.matchers;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.wei.jtrace.api.clazz.MethodDescriber;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcher;
import com.github.wei.jtrace.core.test.BaseTest;
import com.github.wei.jtrace.core.transform.matchers.MethodArgumentMatcher;
import com.github.wei.jtrace.core.util.ClazzUtil;

public class MethodArgumentMatcherTest extends BaseTest{

	@Test
	public void testArgument() {
		IMethodMatcher methodMatcher = new MethodArgumentMatcher("test", 2);
		
		MethodDescriber methodDescriber = ClazzUtil.extractMethodDescriber(0x1, "test", "(Ljava/lang/String;Ljava/lang/Integer;)V", null);

		assertTrue(methodMatcher.match(methodDescriber));
	}
	
	@Test
	public void testRegex() {
		IMethodMatcher methodMatcher = new MethodArgumentMatcher("^test.*", 2);
		
		MethodDescriber methodDescriber = ClazzUtil.extractMethodDescriber(0x1, "test", "(Ljava/lang/String;Ljava/lang/Integer;)V", null);

		assertTrue(methodMatcher.match(methodDescriber));
	}
	
}
