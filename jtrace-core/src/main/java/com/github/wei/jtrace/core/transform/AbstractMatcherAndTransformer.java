package com.github.wei.jtrace.core.transform;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.clazz.MethodDescriber;
import com.github.wei.jtrace.api.exception.ClassMatchException;
import com.github.wei.jtrace.api.transform.ITransformer;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.IMatchedListener;
import com.github.wei.jtrace.api.transform.matcher.IMethodMatcherWithContext;
import com.github.wei.jtrace.api.transform.matcher.MatcherContext;
import com.github.wei.jtrace.core.transform.matchers.Matcher;
import com.github.wei.jtrace.core.transform.matchers.OrClassMatcher;
import com.github.wei.jtrace.core.util.ClazzUtil;
import com.github.wei.jtrace.core.util.Constants;

public abstract class AbstractMatcherAndTransformer implements ITransformer{
	private static Logger log = LoggerFactory.getLogger("MatcherAndTransformer");

	private ConcurrentHashMap<Integer, List<Matcher> > groupMatchers = new ConcurrentHashMap<Integer, List<Matcher>>();

	public abstract byte[] matchedTransform(final ClassLoader loader, IClassDescriberTree descr, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer, Set<MatchedMethod> matchedMethods) throws IllegalClassFormatException;
	
	@Override
	public byte[] transform(ClassLoader loader, IClassDescriberTree descr, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		ClassReader classReader = new ClassReader(classfileBuffer);
		List<MethodDescriber> methods = ClazzUtil.extractMethodDescribers(classReader);
		
		Set<MatchedMethod> matchedMethods = new HashSet<MatchedMethod>();
		try {
			if(isMatched(descr, methods, matchedMethods)) {
				return matchedTransform(loader, descr, classBeingRedefined, protectionDomain, classfileBuffer, matchedMethods);
			}
			
		}catch (ClassMatchException e) {
			log.error("Match class " + descr.getClassDescriber().getName() +" failed", e);
		}
		
		return null;
	}

	private List<MatchedMethod> convertToMatchedMethod(int groupId, Collection<MethodDescriber> methods, MatcherContext context){
		List<MatchedMethod> result = new ArrayList<MatchedMethod>();
		
		for(MethodDescriber md : methods) {
			result.add(createMatchedMethod(groupId, md, context));
		}
		return result;
	}
	
	private boolean isMatched(IClassDescriberTree descr, List<MethodDescriber> methods, Set<MatchedMethod> matchResult) throws ClassMatchException{
		if(groupMatchers.isEmpty()) {
			return false;
		}
		
		boolean matched = false;
		Set<MatchedMethod> groupMatchResult = new HashSet<MatchedMethod>();
		
		for(Map.Entry<Integer,List<Matcher>> matcherEntry: groupMatchers.entrySet()) {
			
			int groupId = matcherEntry.getKey();
			List<Matcher> matchers = matcherEntry.getValue(); 
			for(Matcher matcher : matchers) {
				if(matcher.matchClass(descr)) {
					boolean innerMatched = true;
					MatcherContext matcherContext = matcher.getContext();
					Set<MatchedMethod> matcherMatchResult = new HashSet<MatchedMethod>();
					List<IMethodMatcherWithContext> methodMatchers = matcher.getMethodMatchers();
					
					//如果MethodMatchers是空，意味着全部适配
					if(methodMatchers.isEmpty()) {
						matched = true;
						matcherMatchResult.addAll(convertToMatchedMethod(groupId, methods, matcher.getContext()));
					}else if(methods.isEmpty()){
						//methodMatchers不是空,但是类里没有方法。说明没有适配
						innerMatched = false;
					}else {
						List<MatchedMethod> matchedMethod = new ArrayList<MatchedMethod>();
						
						for(IMethodMatcherWithContext methodMatcher : methodMatchers) {
							List<MethodDescriber> innerMatchedMethod = new ArrayList<MethodDescriber>();
							
							for(MethodDescriber m : methods) {
								if(methodMatcher.match(m)) {
									innerMatchedMethod.add(m);
								}
							}
							if(innerMatchedMethod.size() == 0) {
								innerMatched = false;
								break;
							}else {
								MatcherContext context = new MatcherContext();
								context.merge(matcherContext);
								context.merge(methodMatcher.getContext());
								
								for(MethodDescriber md : innerMatchedMethod) {
									matchedMethod.add(createMatchedMethod(groupId, md, context));
								}
							}
						}
						if(innerMatched) {
							matched = true;
							matcherMatchResult.addAll(matchedMethod);
						}
					}
					
					if(!innerMatched) {
						continue;
					}
					
					IMatchedListener matchedListener = matcher.getMatchedListener();
					if(matchedListener != null) {
						Set<MethodDescriber> mds = new HashSet<MethodDescriber>();
						for(MatchedMethod method : matcherMatchResult) {
							MethodDescriber md = method.getMethodDescriber();
							mds.add(md);
						}
						matchedListener.matched(descr.getClassDescriber(), mds);
					}
					
					//不收录不嵌码的适配结果
					if(!Constants.MATCHER_CONTEXT_WEAVE_NONE.equals(matcherContext.get(Constants.MATCHER_CONTEXT_WEAVE))) {
						
						//重新合并
						for(MatchedMethod mm : matcherMatchResult) {
							boolean found = false;
							for(MatchedMethod gm : groupMatchResult) {
								if(gm.equals(mm)) {
									found = true;
									gm.getContext().merge(mm.getContext());
									break;
								}
							}
							if(!found) {
								groupMatchResult.add(mm);
							}
						}
					}
					
				}
			}
		}
		
		matchResult.addAll(groupMatchResult);
		
		return matched;
	}
	
	protected MatchedMethod createMatchedMethod(int groupid, MethodDescriber md, MatcherContext context) {
		return new MatchedMethod(md, context);
	}
	
	@Override
	public boolean matchClass(IClassDescriberTree descr) {
		for(Map.Entry<Integer,List<Matcher>> matcherEntry: groupMatchers.entrySet()) {
			List<Matcher> matchers = matcherEntry.getValue(); 
			for(Matcher matcher : matchers) {
				try {
					if(matcher.matchClass(descr)) {
						return true;
					}
				}catch(ClassMatchException e) {
					log.error("Match class " + descr.getClassDescriber().getName() + " failed", e);
				}
			}
		}
		return false;
	}
	
	public void addMatcher(int groupId, Matcher matcher) {
		List<Matcher> matchers = groupMatchers.get(groupId);
		if(matchers == null) {
			groupMatchers.putIfAbsent(groupId, new CopyOnWriteArrayList<Matcher>());
			matchers = groupMatchers.get(groupId);
		}
		matchers.add(matcher);
	}
	
	/**
	 * 移除了Matcher,如果要恢复Matcher适配的嵌码类。可以用返回的IClassMatcher进行refresh.
	 * 如果没有找到Matcher，则返回NULL
	 * @param groupId
	 * @param id
	 */
	public IClassMatcher removeMatcher(int groupId, long id) {
		List<Matcher> matchers = groupMatchers.get(groupId);
		if(matchers == null) {
			return null;
		}
		
		List<Matcher> removed = new ArrayList<Matcher>();
		for(Matcher m : matchers) {
			if(m.getId() == id) {
				removed.add(m);
			}
		}
		
		if(matchers.size() == removed.size()) {
			groupMatchers.remove(groupId);
		}else {
			matchers.removeAll(removed);
		}
		
		if(!removed.isEmpty()) {
			return new OrClassMatcher(removed);
		}
		return null;
	}
	
	public IClassMatcher getMatcher(int groupId, long id) {
		List<Matcher> matchers = groupMatchers.get(groupId);
		if(matchers == null) {
			return null;
		}
		
		List<Matcher> result = new ArrayList<Matcher>();
		for(Matcher m : matchers) {
			if(m.getId() == id) {
				result.add(m);
			}
		}
		
		if(!result.isEmpty()) {
			return new OrClassMatcher(result);
		}
		
		return null;
	}
	
	/**
	 * 如果没有找到Matcher，则返回NULL
	 * @param groupId
	 * @return
	 */
	public IClassMatcher getGroupMatcher(int groupId){
		List<Matcher> matchers = groupMatchers.get(groupId);
		if(matchers != null) {
			return new OrClassMatcher(matchers);
		}
		return null;
	}
	
	/**
	 * 根据groupId，删除Matcher
	 * 如果没有找到Matcher，则返回NULL
	 * @param groupId
	 * @return
	 */
	public IClassMatcher removeGroupClassMatcherById(int groupId) {
		List<Matcher> matchers = groupMatchers.remove(groupId);
		if(matchers != null) {
			return new OrClassMatcher(matchers);
		}
		return null;
	}
}
