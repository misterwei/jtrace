package com.github.wei.jtrace.core.extension;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ExtensionJarInfo {
	private File file;
	private Map<String, String> attributes;
	
	private String jarPath;
	private long lastModified;
	
	private ExtensionJarInfo(File file) throws IOException{
		this.file = file;
		this.jarPath = file.getAbsolutePath();
		this.lastModified = file.lastModified();
		
		Map<String,String> inner_attrs = new HashMap<String, String>();
		JarFile jarFile = null;
		try{
			jarFile = new JarFile(file);
			Manifest manifest = jarFile.getManifest();
			Attributes attrs = manifest.getMainAttributes();
			Set<Object> keys = attrs.keySet();
			for(Object key : keys){
				String name = key.toString();
				inner_attrs.put(name, attrs.getValue(name));
			}
			
			attributes = Collections.unmodifiableMap(inner_attrs);
			
		}finally{
			if(jarFile != null){
				jarFile.close();
			}
		}
		
	}
	
	public static ExtensionJarInfo create(File file) throws IOException  {
		return new ExtensionJarInfo(file);
	}
	
	public Map<String,String> getAttributes(){
		return attributes;
	}
	
	public String getJarPath(){
		return file.getAbsolutePath();
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
