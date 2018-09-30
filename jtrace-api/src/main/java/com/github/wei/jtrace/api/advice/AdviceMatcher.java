package com.github.wei.jtrace.api.advice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.wei.jtrace.api.transform.matcher.IMatchedListener;
import com.github.wei.jtrace.api.transform.matcher.MatcherContext;

public class AdviceMatcher {
	public static enum MatchType{
		EXTRACT,
		BASE,
		INTERFACE
	}
	
	private long id;
	private String matchClassName;
	private String matchAnnotation;
	private MatchType matchType;
	private boolean relateParent;
	private IMatchedListener matchedListener;
	private String message;
	
	private MatcherContext context = new MatcherContext();
	private List<MethodAdviceMatcher> methodAdviceMatchers = new ArrayList<MethodAdviceMatcher>();
	
	private AdviceMatcher() {}
	
	public long getId() {
		return this.id;
	}
	
	public String getMatchClassName() {
		return matchClassName;
	}
	
	public String getMatchAnnotation() {
		return matchAnnotation;
	}

	public String getMessage() {
		return message;
	}
	
	public MatchType getMatchType() {
		return matchType;
	}

	public MatcherContext getContext() {
		return this.context;
	}
	
	public boolean isRelateParent() {
		return relateParent;
	}

	public List<MethodAdviceMatcher> getMatchMethods() {
		return methodAdviceMatchers;
	}

	public IMatchedListener getMatchedListener() {
		return matchedListener;
	}
	
	public static Builder newBuilderForClassName(String className) {
		return new Builder(className, null);
	}
	
	public static Builder newBuilderForAnnotation(String annotation) {
		return new Builder(null, annotation);
	}
	
	public static Builder newBuilder(String className, String annotation) {
		return new Builder(className, annotation);
	}
	
	public static class MethodAdviceMatcher{
		private String name;
		private String annotation;
		private Map<String, Object> params = new HashMap<String, Object>();
		
		public String getName() {
			return name;
		}
		public String getAnnotation() {
			return annotation;
		}
		
		public Object getParameter(String name) {
			return params.get(name);
		}

		public Map<String, Object> getParameters(){
			return params;
		}
	}
	
	public static class Builder {
		protected AdviceMatcher matcher;
		public Builder(String className, String annotation) {
			matcher = new AdviceMatcher();
			matcher.matchClassName = className;
			matcher.matchAnnotation = annotation;
			matcher.matchType = MatchType.EXTRACT;
			matcher.id = System.currentTimeMillis();
		}
		
		public Builder matchType(MatchType matchType) {
			matcher.matchType = matchType;
			return this;
		}
		
		public Builder setMessage(String message) {
			matcher.message = message;
			return this;
		}
		
		public Builder withId(long id) {
			matcher.id = id;
			return this;
		}
		
		public Builder noWeave() {
			matcher.context.put("context_weave", "none");
			return this;
		}
		
		public Builder rewriteArgs() {
			matcher.context.put("context_rewrite_args", true);
			return this;
		}
		
		public Builder relateParent() {
			matcher.relateParent = true;
			return this;
		}
		
		public Builder withMatchedListener(IMatchedListener listener) {
			matcher.matchedListener = listener;
			return this;
		}
		
		public BuilderForMethod addMethod() {
			MethodAdviceMatcher mam = new MethodAdviceMatcher();
			return new BuilderForMethod(this, mam);
		}
		
		public AdviceMatcher build() {
			return matcher;
		}
		
		public static class BuilderForMethod{
			private Builder parent;
			private MethodAdviceMatcher mam;
			public BuilderForMethod(Builder parent, MethodAdviceMatcher mam) {
				this.parent = parent;
				this.mam = mam;
			}
			
			public BuilderForMethod matchName(String method) {
				this.mam.name = method;
				return this;
			}
			
			public BuilderForMethod matchAnnotation(String annotation) {
				this.mam.annotation = annotation;
				return this;
			}
			
			public BuilderForMethod trace() {
				this.mam.params.put("context_trace", true);
				return this;
			}
			
			public Builder end() {
				if(this.mam.name == null && this.mam.annotation == null) {
					return parent;
				}
				
				parent.matcher.methodAdviceMatchers.add(mam);
				return parent;
			}
		}
	}
	

	
}
