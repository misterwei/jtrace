package com.github.wei.jtrace.core.transform.command;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.command.Argument;
import com.github.wei.jtrace.api.command.ICommand;
import com.github.wei.jtrace.api.transform.ITransformService;
import com.github.wei.jtrace.api.transform.matcher.BaseClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.ExtractClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.InterfaceClassMatcher;
import com.github.wei.jtrace.core.transform.MatchAndRestoreService;

import java.io.Serializable;

@Bean
public class RefreshClassCommand implements ICommand{

	@AutoRef
	private ITransformService transformService;
	
	@Override
	public String name() {
		return "refreshClass";
	}

	@Override
	public Serializable execute(Object... args) throws Exception {
		String className = String.valueOf(args[0]).replaceAll("\\.", "/");
		String matchType = String.valueOf(args[1]);
		
		IClassMatcher classMatcher = extractClassMatcher(className, matchType);
		transformService.refreshTransformer(classMatcher);
		
		return "ok";
	}

	protected IClassMatcher extractClassMatcher(String className, String matchType) throws Exception {
		if("extract".equals(matchType)) {
			return new ExtractClassMatcher(className);
		}else if("base".equals(matchType)) {
			return new BaseClassMatcher(className);
		}else if("interface".equals(matchType)) {
			return new InterfaceClassMatcher(className);
		}
		throw new IllegalArgumentException("不能识别的类适配方式 ：" + matchType);
	}
	
	@Override
	public String introduction() {
		return "刷新Class";
	}

	@Override
	public Argument[] args() {
		return new Argument[]{new Argument("class", "类名，需要全路径", true, String.class),
				new Argument("matchType", "适配类型", String.class, "extract")};

	}

}
