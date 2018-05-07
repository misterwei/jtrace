package com.github.wei.jtrace.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;

public class AgentHelper {
	public static final Boolean SYSTEM_OUT = false;
	public static final String AGENT_JAR_NAME = "jtrace-core.jar";
	public static final ClassLoader AGENT_CLASSLOADER = AgentHelper.class.getClassLoader();
	
	private static File agentDir = null;
	
	static{
		if(SYSTEM_OUT) {
			System.out.println("jtrace classLoader: " + AGENT_CLASSLOADER);
		}
		
		if(AGENT_CLASSLOADER instanceof URLClassLoader){
			URL[] urls = ((URLClassLoader) AGENT_CLASSLOADER).getURLs();
			if(urls != null){
				for(URL url : urls){
					if(SYSTEM_OUT) {
						System.out.println("lib url: " + url);
					}
					
					if(url.getFile().endsWith(AGENT_JAR_NAME)){
						String name = getAgentJarFileName(url);
						if(name != null){
							agentDir = new File(name).getParentFile();
						}
					}
				}
			}
		}
	}
	
	public static URL getAgentPropertiesFile() throws Exception {
		String path = System.getProperty("jtrace.properties.file");
		if(path != null) {
			File propertyFile = new File(path);
			return propertyFile.toURI().toURL();
		}
		
		File propertyFile = new File(agentDir, "jtrace.properties");
		if(propertyFile.exists()) {
			return propertyFile.toURI().toURL();
		}
		
		URL url = AGENT_CLASSLOADER.getResource("jtrace.properties");
		if(url != null) {
			return url;
		}
		
		throw new FileNotFoundException("jtrace.properties not found");
	}
	
	public static File getAgentDirectory(){
		return agentDir;
	}
	
	private static String getAgentJarFileName(URL agentJarUrl) {
		if (agentJarUrl == null)
			return null;
		try {
			return URLDecoder.decode(agentJarUrl.getFile().replace("+", "%2B"), "UTF-8");
		} catch (IOException e) {
		}
		return null;
	}
}
