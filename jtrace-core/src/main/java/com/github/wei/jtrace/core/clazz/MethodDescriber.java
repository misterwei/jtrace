package com.github.wei.jtrace.core.clazz;

import java.io.Serializable;
import java.util.Arrays;

import org.objectweb.asm.Type;

import com.github.wei.jtrace.core.util.ModifierUtil;

public class MethodDescriber implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String name;
	private String modifier;
	private String[] argumentTypes;
	private String returnType;
	
	private int access;
	
	private String descriptor;
	
	public MethodDescriber(String name, String desc, int access) {
		this.access = access;
		this.name = name;
		this.descriptor = desc;
		
		desc = desc.replace('.', '/');
		Type[] types = Type.getArgumentTypes(desc);
		if(types != null) {
			argumentTypes = new String[types.length]; 
			for(int i=0;i< types.length;i++) {
				argumentTypes[i] = types[i].getClassName().replace('.', '/');
			}
		}
		this.returnType = Type.getReturnType(desc).getClassName().replace('.', '/');
		this.modifier = ModifierUtil.toString(access);
	}
	
	public int getAccess() {
		return access;
	}
	
	public String getDescriptor() {
		return descriptor;
	}
	
	public String getName() {
		return name;
	}
	public String getModifier() {
		return modifier;
	}

	public String getReturnType() {
		return returnType;
	}

	public String[] getArgumentTypes() {
		return argumentTypes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(argumentTypes);
		result = prime * result + ((modifier == null) ? 0 : modifier.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodDescriber other = (MethodDescriber) obj;
		if (!Arrays.equals(argumentTypes, other.argumentTypes))
			return false;
		if (modifier == null) {
			if (other.modifier != null)
				return false;
		} else if (!modifier.equals(other.modifier))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MethodDescriber[");
		sb.append(modifier).append(" ").append(name).append(descriptor).append("]");
		return sb.toString();
	}
}
