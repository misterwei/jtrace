package com.github.wei.jtrace.api.advice;

import java.util.ArrayList;
import java.util.List;

public class AdviceMatcher {
	public static enum MatchType{
		EXTRACT,
		BASE,
		INTERFACE
	}
	
	private String className;
	private MatchType matchType;
	private boolean relateParent;
	private List<String> methods = new ArrayList<String>();
	
	private AdviceMatcher() {}
	
	public String getClassName() {
		return className;
	}

	public MatchType getMatchType() {
		return matchType;
	}

	public boolean isRelateParent() {
		return relateParent;
	}

	public List<String> getMethods() {
		return methods;
	}

	public static Builder newBuilder(String className) {
		return new Builder(className);
	}
	
	public static class Builder {
		protected AdviceMatcher matcher;
		public Builder(String className) {
			matcher = new AdviceMatcher();
			matcher.className = className;
			matcher.matchType = MatchType.EXTRACT;
		}
		
		public Builder matchType(MatchType matchType) {
			matcher.matchType = matchType;
			return this;
		}
		
		
		public Builder relateParent() {
			matcher.relateParent = true;
			return this;
		}
		
		
		public Builder addMethod(String method) {
			matcher.methods.add(method);
			return this;
		}
		
		public AdviceMatcher build() {
			return matcher;
		}
	}
	

	
}
