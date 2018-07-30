package com.github.wei.jtrace.core.transform.command;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;
import com.github.wei.jtrace.api.transform.ITransformer;
import com.github.wei.jtrace.core.transform.TransformService;
import com.github.wei.jtrace.core.transform.matchers.IQueryMatchResult;

@Bean
public class QueryMatchResultCommand implements ICommand{

	@AutoRef
	private TransformService advisorWeaveService;
	
	@Override
	public String name() {
		return "queryMatch";
	}

	@Override
	public Serializable execute(Object... args) throws Exception {
		int id = Integer.parseInt(String.valueOf(args[0]));
		ITransformer matcherAndTransformer = advisorWeaveService.getTransformer(id);
		if(matcherAndTransformer == null) {
			throw new Exception("MatcherAndTransformer "+id+" not found");
		}
		
		if(!(matcherAndTransformer instanceof IQueryMatchResult)) {
			throw new Exception("MatcherAndTransformer "+id+" not support query match result");
		}
		
		Map<ClassLoader, List<ClassDescriber> > matchedClasses = ((IQueryMatchResult)matcherAndTransformer).getMatchedClasses();
		HashMap<ClassLoader,Object> result = new HashMap<ClassLoader, Object>();
		result.putAll(matchedClasses);
		return result;
	}

	@Override
	public String introduction() {
		return "query matched class";
	}

	@Override
	public Argument[] args() {
		return new Argument[]{new Argument("id", "matcher id", true, Integer.class)};
	}

}
