package com.github.wei.jtrace.core.test.matchers;

import java.io.Serializable;

import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcher;
import com.github.wei.jtrace.core.transform.command.AbstractClassMatchCommand;

@Bean
public class TestClassMatchCommand extends AbstractClassMatchCommand{

	@Override
	public String name() {
		return "test";
	}

	@Override
	public String introduction() {
		return "test match command";
	}

	@Override
	protected Serializable doMatch(IClassMatcher classMatcher, IMethodMatcher... matchers) {
		return "ok";
	}

}
