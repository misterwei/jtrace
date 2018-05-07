package com.github.wei.jtrace.core.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;
import com.github.wei.jtrace.api.exception.CommandExecuteException;
import com.github.wei.jtrace.api.resource.IResource;
import com.github.wei.jtrace.api.resource.IResourceSearcher;

@Bean
public class SearchResourceCommand implements ICommand {

	@AutoRef
	private IResourceSearcher resourceSearcher;
	
	@Override
	public String name() {
		return "search";
	}

	@Override
	public Serializable execute(Object... args) throws Exception {
		try{
			String clazz = args[0].toString();
			String from = args[1].toString();
			
			List<IResource> resList = null;
			if("default".equalsIgnoreCase(from)){
				IResource res = resourceSearcher.searchResource(clazz);
				if(res != null) {
					resList = new ArrayList<IResource>(1);
					resList.add(res);
				}else {
					resList = Collections.emptyList();
				}
			}else if("all".equalsIgnoreCase(from)){
				resList = resourceSearcher.searchResourceFromAll(clazz);
			}else if("tree".equalsIgnoreCase(from)){
				resList = resourceSearcher.searchResourceFromTree(clazz);
			}
			
			ArrayList<HashMap<String,String>> paths = new ArrayList<HashMap<String,String>>();
			if(resList != null){
				for(IResource r : resList){
					HashMap<String,String> res = new HashMap<String, String>();
					if(r.getURL() != null){
						res.put("path", r.getURL().toString());
					}
					res.put("classLoader", String.valueOf(r.getClassLoader()));
					paths.add(res);
				}
			}
			
			return paths;
		}catch(Exception e){
			e.printStackTrace();
			throw new CommandExecuteException("resource", e);
		}
	}

	@Override
	public String introduction() {
		return "search classpath resource";
	}

	@Override
	public Argument[] args() {
		return new Argument[]{new Argument("resource", "classpath resource", true, String.class),
				new Argument("from", "从哪里搜索，default, all, tree", String.class, "default")};
	}

}
