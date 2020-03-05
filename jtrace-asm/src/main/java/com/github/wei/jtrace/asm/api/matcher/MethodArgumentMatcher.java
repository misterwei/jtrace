package com.github.wei.jtrace.asm.api.matcher;

import com.github.wei.jtrace.asm.util.StringUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

public class MethodArgumentMatcher implements IMethodMatcher{
	
	private String name;
	private int args;
	public MethodArgumentMatcher(String name, int args) {
		this.name = name;
		this.args = args;
	}
	
	@Override
	public boolean match(MethodNode target) {
		return StringUtil.match(name, target.name) && Type.getArgumentTypes(target.desc).length == args;
	}

}
