package com.github.wei.jtrace.core.transform.command;

import java.io.Serializable;
import java.util.HashMap;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.transform.ITransformerMatcher;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcher;
import com.github.wei.jtrace.core.transform.ClassMatcherAndResult;
import com.github.wei.jtrace.core.transform.TransformService;

@Bean
public class MatchClassCommand extends AbstractClassMatchCommand{

	@AutoRef
	private TransformService advisorWeaveService;
	
	@Override
	public String name() {
		return "match";
	}

	@Override
	protected Serializable doMatch(IClassMatcher classMatcher, IMethodMatcher... matchers) throws Exception{
		
		ITransformerMatcher matcherAndTransformer = null;
		if(matchers == null) {
			matcherAndTransformer =	new ClassMatcherAndResult(classMatcher);
		}else {
			matcherAndTransformer =	new ClassMatcherAndResult(classMatcher, matchers);
		}
		
		int id = advisorWeaveService.registerTransformerMatcher(matcherAndTransformer,true);
		HashMap<String,Object> result = new HashMap<String, Object>();
		result.put("id", id);
		return result;
	}

	@Override
	public String introduction() {
		return "match class";
	}

	
}
