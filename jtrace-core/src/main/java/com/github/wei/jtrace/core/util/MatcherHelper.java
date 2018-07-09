package com.github.wei.jtrace.core.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.wei.jtrace.api.matcher.BaseClassMatcher;
import com.github.wei.jtrace.api.matcher.ExtractClassMatcher;
import com.github.wei.jtrace.api.matcher.IClassMatcher;
import com.github.wei.jtrace.api.matcher.InterfaceClassMatcher;
import com.github.wei.jtrace.core.transform.matchers.IMethodMatcher;
import com.github.wei.jtrace.core.transform.matchers.MethodArgumentMatcher;
import com.github.wei.jtrace.core.transform.matchers.MethodExtractMatcher;
import com.github.wei.jtrace.core.transform.matchers.MethodNameMatcher;
import com.google.common.base.Splitter;

public class MatcherHelper {
	
	/**
	 * 转换为ClassMatcher， className自动替换 '.' 为 '/' 
	 * @param className
	 * @param matchType
	 * @return
	 * @throws Exception
	 */
	public static IClassMatcher extractClassMatcher(String className, String matchType) throws Exception {
		className = ClazzUtil.classNameToPath(className);
		if("extract".equals(matchType)) {
			return new ExtractClassMatcher(className);
		}else if("base".equals(matchType)) {
			return new BaseClassMatcher(className);
		}else if("interface".equals(matchType)) {
			return new InterfaceClassMatcher(className);
		}
		throw new IllegalArgumentException("不能识别的类适配方式 ：" + matchType);
	}
	
	public static IClassMatcher extractClassMatcher(String[] className, String matchType) throws Exception {
		className = ClazzUtil.classNameToPath(className);
		if("extract".equals(matchType)) {
			return new ExtractClassMatcher(className);
		}else if("base".equals(matchType)) {
			return new BaseClassMatcher(className);
		}else if("interface".equals(matchType)) {
			return new InterfaceClassMatcher(className);
		}
		throw new IllegalArgumentException("不能识别的类适配方式 ：" + matchType);
	}
	
	public static IMethodMatcher[] extractMethodMatchers(String methodStr) {
		IMethodMatcher[] methodMatchers = null;
		if(methodStr != null) {
			List<String> methods = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(methodStr);
			if(methods != null && methods.size() > 0) {
				methodMatchers = new IMethodMatcher[methods.size()];
				
				for(int i=0;i<methods.size();i++) {
					methodMatchers[i] = extractMethodMatcher(methods.get(i));
				}
			}
		}
		return methodMatchers;
	}
	
	public static  IMethodMatcher extractMethodMatcher(String method) {
		if(method.contains("(")) {
			int index = method.indexOf("(");
			String methodName = method.substring(0, index);
			String methodDescr = method.substring(index);
			if(methodDescr.matches("\\(\\d+\\)")) {
				Pattern p = Pattern.compile("\\((\\d+)\\)");
				Matcher matcher = p.matcher(methodDescr);
				int args = 0;
				if(matcher.find()) {
					args = Integer.parseInt(matcher.group(1));
				}
				return new MethodArgumentMatcher(methodName, args);
			}else {
				return new MethodExtractMatcher(methodName,methodDescr);
			}
			
		}else {
			return new MethodNameMatcher(method);
		}
	}
}
