package com.github.wei.jtrace.asm.test;

import com.github.wei.jtrace.asm.api.matcher.IMethodMatcher;
import com.github.wei.jtrace.asm.api.matcher.MethodArgumentMatcher;
import com.github.wei.jtrace.core.test.BaseTest;
import org.junit.Test;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MethodArgumentMatcherTest extends BaseTest{

	@Test
	public void testArgument() {
		IMethodMatcher methodMatcher = new MethodArgumentMatcher("test", 2);
		
		MethodNode mn = new MethodNode(0x1, "test", "(Ljava/lang/String;Ljava/lang/Integer;)V", null, null);
		assertTrue(methodMatcher.match(mn));
	}

	@Test
	public void testNoArgument() {
		IMethodMatcher methodMatcher = new MethodArgumentMatcher("test", 2);

		MethodNode mn = new MethodNode(0x1, "test", "()V", null, null);
		assertFalse(methodMatcher.match(mn));
	}

	@Test
	public void testRegex() {
		IMethodMatcher methodMatcher = new MethodArgumentMatcher("^test.*", 2);

		MethodNode mn = new MethodNode(0x1, "test", "(Ljava/lang/String;Ljava/lang/Integer;)V", null, null);

		assertTrue(methodMatcher.match(mn));
	}
	
}
