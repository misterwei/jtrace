package com.github.wei.jtrace.core.test.matchers;

import java.io.Serializable;

import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.matcher.IClassMatcher;
import com.github.wei.jtrace.core.transform.command.AbstractClassMatchCommand;
import com.github.wei.jtrace.core.transform.matchers.IMethodMatcher;

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
