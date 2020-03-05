package com.github.wei.jtrace.asm.api.matcher;

import com.github.wei.jtrace.asm.util.StringUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

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
	public boolean match(MethodNode target) {
		if(!StringUtil.match(methodName, target.name)){
			return false;
		}
		if(!returnType.equals(Type.getReturnType(target.desc).getClassName().replace(".", "/"))) {
			return false;
		}

		Type[] parameterTypes = Type.getArgumentTypes(target.desc);

		int parameterSize = parameterTypes == null ? 0 : parameterTypes.length;
		if(argumentTypes.length != parameterSize) {
			return false;
		}
		for(int i=0;i<argumentTypes.length;i++) {
			if(argumentTypes[i] == null || !argumentTypes[i].equals(parameterTypes[i].getInternalName())) {
				return false;
			}
		}
		return true;
	}

}
