package com.github.wei.jtrace.asm.api.matcher;

import com.github.wei.jtrace.asm.util.StringUtil;
import org.objectweb.asm.tree.MethodNode;

public class MethodNameMatcher implements IMethodMatcher{

	private String name;
	public MethodNameMatcher(String name) {
		this.name = name;
	}
	
	@Override
	public boolean match(MethodNode target) {
		return StringUtil.match(name, target.name);
	}

}
