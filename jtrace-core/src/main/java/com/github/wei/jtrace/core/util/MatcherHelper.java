package com.github.wei.jtrace.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcher;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcherWithContext;
import com.github.wei.jtrace.core.transform.matchers.BaseClassMatcher;
import com.github.wei.jtrace.core.transform.matchers.ExtractClassMatcher;
import com.github.wei.jtrace.core.transform.matchers.InterfaceClassMatcher;
import com.github.wei.jtrace.core.transform.matchers.MethodArgumentMatcher;
import com.github.wei.jtrace.core.transform.matchers.MethodExtractMatcher;
import com.github.wei.jtrace.core.transform.matchers.MethodMatcherWithContext;
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
	
	public static List<IMethodMatcher> extractMethodMatchers(List<String> methods) {
		List<IMethodMatcher> methodMatchers = new ArrayList<IMethodMatcher>();
		for(String method : methods) {
			List<IMethodMatcher> ms = extractMethodMatchers(method);
			if(ms != null) {
				methodMatchers.addAll(ms);
			}
		}
		return methodMatchers;
	}
	
	public static List<IMethodMatcherWithContext> extractMethodMatchers(Map<String, Map<String, Object>> methods) {
		List<IMethodMatcherWithContext> methodMatchers = new ArrayList<IMethodMatcherWithContext>();
		
		for(Map.Entry<String, Map<String, Object>> mm: methods.entrySet()) {
			List<IMethodMatcherWithContext> ms = extractMethodMatchers(mm.getKey(), mm.getValue());
			if(ms != null) {
				methodMatchers.addAll(ms);
			}
		}
		return methodMatchers;
	}
	
	public static List<IMethodMatcherWithContext> extractMethodMatchers(String methodStr, Map<String, Object> params) {
		List<IMethodMatcherWithContext> methodMatchers = null;
		if(methodStr != null) {
			List<String> methods = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(methodStr);
			if(methods != null && methods.size() > 0) {
				methodMatchers = new ArrayList<IMethodMatcherWithContext>(methods.size());
				
				for(int i=0;i<methods.size();i++) {
					methodMatchers.add(extractMethodMatcher(methods.get(i), params));
				}
			}
		}
		return methodMatchers;
	}
	
	public static List<IMethodMatcher> extractMethodMatchers(String methodStr) {
		List<IMethodMatcher> methodMatchers = null;
		if(methodStr != null) {
			List<String> methods = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(methodStr);
			if(methods != null && methods.size() > 0) {
				methodMatchers = new ArrayList<IMethodMatcher>(methods.size());
				
				for(int i=0;i<methods.size();i++) {
					methodMatchers.add(extractMethodMatcher(methods.get(i)));
				}
			}
		}
		return methodMatchers;
	}
	
	public static IMethodMatcherWithContext extractMethodMatcher(String method, Map<String, Object> params) {
		IMethodMatcher matcher = extractMethodMatcher(method);
		IMethodMatcherWithContext matcherWithContext = new MethodMatcherWithContext(matcher);
		matcherWithContext.getContext().putAll(params);
		return matcherWithContext;
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
