package com.github.wei.jtrace.api.command;

import java.io.Serializable;


public class Argument implements Serializable{

	private static final long serialVersionUID = 1L;
	private String name;
	private String description;
	private boolean isNecessary = false;
	private Class<?> type;
	private Object defaultValue;
	
	public Argument(String name, String description, boolean isNecessary, Class<?> type) {
		this(name, description, type, null);
		this.isNecessary = isNecessary;
	}
	
	public Argument(String name, String description,  Class<?> type, Object defaultValue) {
		this.name = name;
		this.description = description;
		this.type = type;
		this.defaultValue = defaultValue;
	}
	
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public boolean isNecessary() {
		return isNecessary;
	}
	public Class<?> getType() {
		return type;
	}
	
	public Object getDefaultValue(){
		return defaultValue;
	}
}