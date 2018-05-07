package com.github.wei.jtrace.advice.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.github.wei.jtrace.advice.ognl.OgnlMemberAccess;

import ognl.Ognl;
import ognl.OgnlContext;

public class OgnlTest {
	
	@Test
	public void testOgnl() throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		Object[] args = new Object[] {"aaa", 123};
		
		OgnlContext ctx = new OgnlContext(null, null, new OgnlMemberAccess(true));
		
		String expr = "#this";
		Object r = Ognl.getValue(expr, ctx, 1);
		System.out.println(r);
	}
}
