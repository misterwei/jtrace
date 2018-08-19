package com.github.wei.jtrace.server;

public class TypeConvertUtils {

	@SuppressWarnings("unchecked")
	public static <T> T convert(String org, Class<T> target) throws IllegalArgumentException{
		
		if(target == Integer.class || target == Integer.TYPE){
			return (T)Integer.valueOf(org);
		}else if(target == Long.class || target == Long.TYPE){
			return (T)Long.valueOf(org);
		}else if(target == Double.class || target == Double.TYPE){
			return (T)Double.valueOf(org);
		}else if(target == Float.class || target == Float.TYPE){
			return (T)Float.valueOf(org);
		}else if(target == Boolean.class || target == Boolean.TYPE){
			return (T)Boolean.valueOf(org);
		}else if(target == String.class){
			return target.cast(org);
		}
		
		throw new IllegalArgumentException("not support target type: " + target.getName());
	}
}
