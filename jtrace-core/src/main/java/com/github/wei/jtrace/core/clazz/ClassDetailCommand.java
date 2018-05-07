package com.github.wei.jtrace.core.clazz;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.BeanRef;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;
import com.github.wei.jtrace.api.resource.IResource;
import com.github.wei.jtrace.api.resource.IResourceSearcher;
import com.github.wei.jtrace.core.util.ClazzUtil;

@Bean
public class ClassDetailCommand implements ICommand{

	@BeanRef(name="instrumentation")
	private Instrumentation inst;
	
	@AutoRef
	private IResourceSearcher resourceSearchService;
	
	@Override
	public String name() {
		return "class";
	}

	@Override
	public Serializable execute(Object... args) throws Exception {
		String name = args[0].toString();
		String from = args[1].toString();
		
		ArrayList<ClassInfo> classes = new ArrayList<ClassInfo>();
		if("loaded".equals(from)){
			Class<?>[] loadedClass = inst.getAllLoadedClasses();
			if(loadedClass != null){
				for(Class<?> clazz : loadedClass){
					if(clazz.getName().equals(name)){
						classes.add(ClazzUtil.extractClassInfo(clazz));
					}
				}
			}
		}else{
			String clazz = name.replaceAll("\\.", "/") + ".class";
			
			List<IResource> resources = null;
			if("all".equalsIgnoreCase(from)){
				resources = resourceSearchService.searchResourceFromAll(clazz);
			}else if("tree".equalsIgnoreCase(from)){
				resources = resourceSearchService.searchResourceFromTree(clazz);
			}
			
			if(resources != null){
				Map<String, ClassInfo> urls = new HashMap<String, ClassInfo>();
				for(IResource resource: resources){
					URL url = resource.getURL();
					if(url == null){
						continue;
					}
					String path = url.toString();
					if(!urls.containsKey(path)){
						InputStream is = url.openStream();
						try{
							ClassReader classReader = new ClassReader(is);
							ClassInfo classInfo  = ClazzUtil.extractClassInfo(classReader);
							classInfo.setClassLoader(String.valueOf(resource.getClassLoader()));
							
							classes.add(classInfo);
							
							urls.put(path, classInfo);
						}finally{
							if(is != null){
								is.close();
							}
						}
					}else{
						ClassInfo classInfo = urls.get(path);
						ClassInfo newClassInfo = (ClassInfo)classInfo.clone();
						newClassInfo.setClassLoader(String.valueOf(resource.getClassLoader()));
						
						classes.add(newClassInfo);
					}
				}
			}
		}
		
		return classes;
	}

	@Override
	public String introduction() {
		return "显示类的详细信息";
	}

	@Override
	public Argument[] args() {
		return new Argument[]{
			new Argument("name", "name of the class", true, String.class),
			new Argument("from", "从哪里搜索 loaded, all, tree", String.class, "loaded")
		};
	}

}
