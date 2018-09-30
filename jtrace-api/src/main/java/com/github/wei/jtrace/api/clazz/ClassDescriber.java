package com.github.wei.jtrace.api.clazz;

import java.util.Arrays;

public class ClassDescriber {
	private String name;
	private String modifier;
	private String[] interfaces;
	private String superClass;
	private String[] annotations;
	
	public ClassDescriber(String name, String modifier, String superClass, String[] interfaces, String[] annotations) {
		this.name = name;
		this.modifier = modifier;
		this.superClass = superClass;
		this.interfaces = interfaces;
		this.annotations = annotations;
	}
	
	public String getName() {
		return name;
	}
	public String getModifier() {
		return modifier;
	}
	public String[] getInterfaces() {
		return interfaces;
	}
	public String getSuperClass() {
		return superClass;
	}
	public String[] getAnnotations() {
		return annotations;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ClassDescriber[");
		sb.append(modifier).append(" ").append(name).append("]");
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(interfaces);
//		result = prime * result + ((modifier == null) ? 0 : modifier.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((superClass == null) ? 0 : superClass.hashCode());
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
		ClassDescriber other = (ClassDescriber) obj;
		if (!Arrays.equals(interfaces, other.interfaces))
			return false;
//		if (modifier == null) {
//			if (other.modifier != null)
//				return false;
//		} else if (!modifier.equals(other.modifier))
//			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (superClass == null) {
			if (other.superClass != null)
				return false;
		} else if (!superClass.equals(other.superClass))
			return false;
		return true;
	}
	

}
