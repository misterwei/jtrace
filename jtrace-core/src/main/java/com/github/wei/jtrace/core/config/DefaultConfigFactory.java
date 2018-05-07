package com.github.wei.jtrace.core.config;

import java.net.URL;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;

import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.config.IConfigFactory;
import com.github.wei.jtrace.core.logger.RootLogger;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

public class DefaultConfigFactory implements IConfigFactory{
	Logger log = RootLogger.get();
	
	private Config config;
	
	private LoadingCache<Class<?>, Object> cache = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, Object>(){
		@Override
		public Object load(Class<?> type) throws Exception {
			com.github.wei.jtrace.api.config.Config cfg = type.getAnnotation(com.github.wei.jtrace.api.config.Config.class);
			Config temp = config;
			if(!Strings.isNullOrEmpty(cfg.value())){
				temp = config.getConfig(cfg.value());
			}
			
			return ConfigHelper.createConfig(temp, type);
		}
		
	}) ;
	
	
	
	public DefaultConfigFactory(String path){
		config = ConfigFactory.load(path);
	}
	
	public DefaultConfigFactory(URL url) {
		config = ConfigFactory.parseURL(url);
	}
	
	public <T> T getConfig(Class<T> clazz) throws Exception{
		return clazz.cast(cache.get(clazz));
	}
	
	public IConfig getConfig(String key){
		try{
			return new DefaultConfig(config.getConfig(key));
		}catch(ConfigException.Missing ex){
			log.debug("can not found key: " + key);
			return EMPTY_CONFIG;
		}
//		catch(ConfigException ex){
//			log.warn("can not found key: " + key);
//			return EMPTY_CONFIG;
//		}
	}
	
	private final IConfig EMPTY_CONFIG = new IConfig(){

		@Override
		public int getInt(String path) {
			return 0;
		}

		@Override
		public long getLong(String path) {
			return 0;
		}

		@Override
		public double getDouble(String path) {
			return 0;
		}

		@Override
		public String getString(String path) {
			return null;
		}

		@Override
		public boolean getBoolean(String path) {
			return false;
		}

		@Override
		public int getInt(String path, int defaultValue) {
			return defaultValue;
		}

		@Override
		public long getLong(String path, long defaultValue) {
			return defaultValue;
		}

		@Override
		public double getDouble(String path, double defaultValue) {
			return defaultValue;
		}

		@Override
		public String getString(String path, String defaultValue) {
			return defaultValue;
		}

		@Override
		public boolean getBoolean(String path, boolean defaultValue) {
			return defaultValue;
		}

		@Override
		public Set<String> keySet() {
			return Collections.emptySet();
		}
		
	};
	
}
