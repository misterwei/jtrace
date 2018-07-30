package com.github.wei.jtrace.core.transform.matchers;

import org.objectweb.asm.Type;

import com.github.wei.jtrace.api.clazz.MethodDescriber;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcher;
import com.github.wei.jtrace.core.util.StringUtil;

public class MethodExtractMatcher implements IMethodMatcher{

	private String[] argumentTypes;
	private String returnType;
	private String methodName;
	public MethodExtractMatcher(String methodName, String methodDescriptor) {
		methodDescriptor = methodDescriptor.replace('.', '/');
		if(methodDescriptor.endsWith(")")) {
			methodDescriptor = methodDescriptor + "V";
		}
		
		Type[] arguments = Type.getArgumentTypes(methodDescriptor);
		
		argumentTypes = new String[arguments.length];
		for(int i=0;i<arguments.length;i++) {
			argumentTypes[i] = StringUtil.replace(arguments[i].getClassName(),'.', '/');
		}
		
		this.returnType = Type.getReturnType(methodDescriptor).getClassName().replace('.', '/');
		this.methodName = methodName;
	}
	
	public String[] getArgumentTypes() {
		return argumentTypes;
	}

	public String getReturnType() {
		return returnType;
	}

	public String getMethodName() {
		return methodName;
	}

	@Override
	public boolean match(MethodDescriber target) {
		if(!StringUtil.match(methodName, target.getName())){
			return false;
		}
		if(!returnType.equals(target.getReturnType())) {
			return false;
		}
		String[] targetArgTypes = target.getArgumentTypes();
		if(argumentTypes.length != targetArgTypes.length) {
			return false;
		}
		for(int i=0;i<argumentTypes.length;i++) {
			if(argumentTypes[i] == null || !argumentTypes[i].equals(targetArgTypes[i])) {
				return false;
			}
		}
		return true;
	}

}
