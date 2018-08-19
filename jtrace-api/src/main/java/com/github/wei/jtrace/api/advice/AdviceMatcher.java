package com.github.wei.jtrace.api.advice;

import java.util.HashMap;
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
	private MatchType matchType;
	private boolean relateParent;
	private IMatchedListener matchedListener;
	private String message;
	
	private MatcherContext context = new MatcherContext();
	private Map<String, Map<String, Object>> matchMethods = new HashMap<String, Map<String, Object>>();
	
	private AdviceMatcher() {}
	
	public long getId() {
		return this.id;
	}
	
	public String getMatchClassName() {
		return matchClassName;
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

	public Map<String, Map<String, Object>> getMatchMethods() {
		return matchMethods;
	}

	public IMatchedListener getMatchedListener() {
		return matchedListener;
	}
	
	public static Builder newBuilder(String className) {
		return new Builder(className);
	}
	
	public static class Builder {
		protected AdviceMatcher matcher;
		public Builder(String className) {
			matcher = new AdviceMatcher();
			matcher.matchClassName = className;
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
		
		public Builder relateParent() {
			matcher.relateParent = true;
			return this;
		}
		
		public Builder withMatchedListener(IMatchedListener listener) {
			matcher.matchedListener = listener;
			return this;
		}
		
		public BuilderForMethod addMethod(String method) {
			Map<String, Object> params = null;
			if(!matcher.matchMethods.containsKey(method)) {
				params = new HashMap<String, Object>();
				matcher.matchMethods.put(method, params);
			}else {
				params = matcher.matchMethods.get(method);
			}
			
			return new BuilderForMethod(this, params);
		}
		
		public AdviceMatcher build() {
			return matcher;
		}
		
		public static class BuilderForMethod{
			private Builder parent;
			private Map<String, Object> params;
			public BuilderForMethod(Builder parent, Map<String, Object> params) {
				this.parent = parent;
				this.params = params;
			}
			
			public BuilderForMethod withTrace() {
				this.params.put("context_trace", true);
				return this;
			}
			
			public Builder end() {
				return parent;
			}
		}
	}
	

	
}
