package com.github.wei.jtrace.core.util;

public class Constants {
	
	public static final String CLASS_THREAD="java/lang/Thread";
	public static final String CLASS_CLASSLOADER="java/lang/ClassLoader";
	public static final String CLASS_OBJECT="java/lang/Object";
	
	public static final String AGENTCLASSLOADER_CLASSNAME="com.github.wei.jtrace.agent.AgentClassLoader";
	public static final String AGENTCLASS_PREFIX = "com.github.wei.jtrace";
	public static final String AGENTCLASS_PATHPREFIX = "com/github/wei/jtrace";
	
	public static final String ADVISOR_WEAVED_CLASS = "Lcom/github/wei/jtrace/agent/AdvisorWeaved;";
	public static final String TRACER_WEAVED_CLASS = "Lcom/github/wei/jtrace/agent/TracerWeaved;";

	public static final String ENABLED = "enabled";
	public static final String DISABLED = "disabled";
}
