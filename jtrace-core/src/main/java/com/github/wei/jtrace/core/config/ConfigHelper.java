package com.github.wei.jtrace.core.config;

import java.lang.reflect.Field;

import com.github.wei.jtrace.api.config.ConfigPath;
import com.github.wei.jtrace.api.config.ConfigValue;
import com.github.wei.jtrace.core.util.TypeConvertUtils;
import com.google.common.base.Strings;
import com.typesafe.config.Config;

public class ConfigHelper {
	
	public static <T> T createConfig(Config config, Class<T> clazz) throws Exception {
		T obj = clazz.newInstance();
		
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields){
			boolean acc = field.isAccessible();
			if(!acc){
				field.setAccessible(true);
			}
			
			String path = field.getName();
			ConfigPath cfgProperty = field.getAnnotation(ConfigPath.class);
			if(cfgProperty == null){
				continue;
			}
			
			if(!Strings.isNullOrEmpty(cfgProperty.value())){
				path = cfgProperty.value();
			}
			
			ConfigValue defaultValue = field.getAnnotation(ConfigValue.class);
			String value = null;
			if(defaultValue != null){
				value = defaultValue.value();
				if(config.hasPath(path)){
					value = config.getString(path);
				}
			}else{
				value = config.getString(path);
			}
		
			Object targetValue = TypeConvertUtils.convert(value, field.getType());
			
			field.set(obj, targetValue);
		}
		
		return obj;
	}
	
	
}
