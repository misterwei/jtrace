package com.github.wei.jtrace.core.config;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import com.github.wei.jtrace.api.config.IConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigValue;

public class DefaultConfig implements IConfig{
	private Config config;
	public DefaultConfig(Config config) {
		this.config = config;
	}

	@Override
	public int getInt(String path) {
		return config.getInt(path);
	}

	@Override
	public long getLong(String path) {
		return config.getLong(path);
	}

	@Override
	public double getDouble(String path) {
		return config.getDouble(path);
	}

	@Override
	public String getString(String path) {
		return config.getString(path);
	}

	@Override
	public boolean getBoolean(String path) {
		return config.getBoolean(path);
	}

	@Override
	public int getInt(String path, int defaultValue) {
		try{
			return config.getInt(path);
		}catch(ConfigException ex){
			return defaultValue;
		}
	}

	@Override
	public long getLong(String path, long defaultValue) {
		try{
			return config.getLong(path);
		}catch(ConfigException ex){
			return defaultValue;
		}
	}

	@Override
	public double getDouble(String path, double defaultValue) {
		try{
			return config.getDouble(path);
		}catch(ConfigException ex){
			return defaultValue;
		}
	}

	@Override
	public String getString(String path, String defaultValue) {
		try{
			return config.getString(path);
		}catch(ConfigException ex){
			return defaultValue;
		}
	}

	@Override
	public boolean getBoolean(String path, boolean defaultValue) {
		try{
			return config.getBoolean(path);
		}catch(ConfigException ex){
			return defaultValue;
		}
	}

	@Override
	public Set<String> keySet() {
		Set<String> keySet = new HashSet<String>();
		
		Set<Entry<String, ConfigValue>> keys = config.entrySet();
		for(Entry<String, ConfigValue> key: keys){
			keySet.add(key.getKey());
		}
		
		return keySet;
	}
}
