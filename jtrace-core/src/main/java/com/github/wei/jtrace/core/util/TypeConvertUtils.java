package com.github.wei.jtrace.core.util;

public class TypeConvertUtils {

	@SuppressWarnings("unchecked")
	public static <T> T convert(String orgStr, Class<T> target) throws Exception{
		
		if(target == Integer.class || target == Integer.TYPE){
			return (T)Integer.valueOf(orgStr);
		}else if(target == Long.class || target == Long.TYPE){
			return (T)Long.valueOf(orgStr);
		}else if(target == Double.class || target == Double.TYPE){
			return (T)Double.valueOf(orgStr);
		}else if(target == Float.class || target == Float.TYPE){
			return (T)Float.valueOf(orgStr);
		}else if(target == Character.class || target == Character.TYPE){
			return (T)Character.valueOf(orgStr.charAt(0));
		}else if(target == Boolean.class || target == Boolean.TYPE){
			return (T)Boolean.valueOf(orgStr);
		}else if(target == String.class){
			return target.cast(orgStr);
		}
		
		throw new IllegalArgumentException("not support target type: " + target.getName());
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> T defaultValue(Class<T> target) throws Exception{
		
		if(target == Integer.TYPE || target == Long.TYPE || target == Double.TYPE || target == Float.TYPE){
			return target.cast(0);
		}else if(target == Boolean.TYPE){
			return (T)Boolean.valueOf(false);
		}else if(target == String.class){
			return null;
		}
		
		return null;
	}
}
