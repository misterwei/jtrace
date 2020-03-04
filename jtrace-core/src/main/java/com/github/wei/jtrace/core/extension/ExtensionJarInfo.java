package com.github.wei.jtrace.core.extension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.yaml.snakeyaml.Yaml;

public class ExtensionJarInfo {
	private File file;
	private Map<String, Object> attributes;
	
	private String jarPath;
	private long lastModified;
	private String name;
	
	private int loadOrder;

	private ExtensionJarInfo(File file) throws Exception{
		this.file = file;
		this.jarPath = file.getAbsolutePath();
		this.lastModified = file.lastModified();
		this.name = file.getName();
		
		JarFile jarFile = null;
		try{
			jarFile = new JarFile(file);

			JarEntry entry = jarFile.getJarEntry("META-INF/jtrace-extension.yaml");
			if(entry != null) {
				InputStream in = jarFile.getInputStream(entry);
				try {
					Yaml yaml = new Yaml();
					Map<String, Object> config = yaml.load(in);
					attributes = Collections.unmodifiableMap(config);
					loadOrder = 0;
					Object order = attributes.get("Load-Order");
					if(order != null){
						loadOrder = Integer.parseInt(String.valueOf(order));
					}
				}finally {
					try {
						in.close();
					}catch(Exception e) {}
				}
			}else {
				attributes = Collections.emptyMap();
				loadOrder = 0;
			}

		}finally{
			if(jarFile != null){
				jarFile.close();
			}
		}
	}
	
	public static ExtensionJarInfo create(File file) throws Exception  {
		return new ExtensionJarInfo(file);
	}
	
	public Map<String,Object> getAttributes(){
		return attributes;
	}
	
	public String getJarPath(){
		return file.getAbsolutePath();
	}

	public File getFile() {
		return file;
	}
	
	public String getName() {
		return name;
	}

	public int getLoadOrder() {
		return loadOrder;
	}

	@Override
	public String toString() {
		return "ExtensionJar ("+getJarPath()+") " + getAttributes();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jarPath == null) ? 0 : jarPath.hashCode());
		result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
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
		ExtensionJarInfo other = (ExtensionJarInfo) obj;
		if (jarPath == null) {
			if (other.jarPath != null)
				return false;
		} else if (!jarPath.equals(other.jarPath))
			return false;
		if (lastModified != other.lastModified)
			return false;
		return true;
	}

}
