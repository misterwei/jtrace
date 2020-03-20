package com.github.wei.jtrace.core.logger;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.config.IConfigFactory;
import com.github.wei.jtrace.core.util.AgentHelperUtil;
import com.google.common.base.Splitter;

public class LoggerConfiger {
	private static ConcurrentHashMap<String, LoggerConfiger> configers = new ConcurrentHashMap<String, LoggerConfiger>();
	private static ConcurrentHashMap<String, IAppender> appenders = new ConcurrentHashMap<String, IAppender>();
	private static Map<String,Integer> levelMap = new HashMap<String, Integer>();
	
	private IAppender appender = null;
	private int level = LoggerFlags.LEVEL_INFO;
	private static File logDir = null;
	
	static{
		File agentDir = AgentHelperUtil.getAgentDirectory();
		if(agentDir == null){
			logDir = new File("logs");
		}else{
			logDir = new File(agentDir, "logs");
		}	
		logDir.mkdirs();
		
		levelMap.put("all", LoggerFlags.LEVEL_ALL);
		levelMap.put("debug", LoggerFlags.LEVEL_DEBUG);
		levelMap.put("error", LoggerFlags.LEVEL_ERROR);
		levelMap.put("fatal", LoggerFlags.LEVEL_FATAL);
		levelMap.put("info", LoggerFlags.LEVEL_INFO);
		levelMap.put("off", LoggerFlags.LEVEL_OFF);
		levelMap.put("trace", LoggerFlags.LEVEL_TRACE);
		levelMap.put("warn", LoggerFlags.LEVEL_WARN);
		
		
		LoggerConfiger rootConfiger = new LoggerConfiger();
		rootConfiger.setAppender(ConsoleAppender.INSTANCE);
		configers.put("ROOT", rootConfiger);
		
	}
	
	public static void clear(){
		Collection<IAppender> appenderValues = appenders.values();
		Iterator<IAppender> it = appenderValues.iterator();
		while(it.hasNext()){
			IAppender appender = it.next();
			appender.close();
			it.remove();
		}
	}
	
	
	@SuppressWarnings("resource")
	public static void autoConfig(IConfigFactory configFactory) throws Exception{
		
		//日志输出
		IConfig config = configFactory.getConfig("log.appender");
		Set<String> appenderKeys = config.keySet();
		Set<String> filter = new HashSet<String>();
		for(String key: appenderKeys){
			if(key.indexOf(".") > 0){
				String appenderName = key.substring(0, key.indexOf("."));
				if(filter.contains(appenderName)){
					continue;
				}
				filter.add(appenderName);
				
				IAppender appender = null;
				
				//输出类型
				String type = config.getString(appenderName+".type", "console");
				if(type.equalsIgnoreCase("console")){
					appender = ConsoleAppender.INSTANCE;
					
				}else if(type.equalsIgnoreCase("file")){
					String path = config.getString(appenderName + ".path", logDir.getAbsolutePath() + File.separator+ appenderName + ".log");
					FileAppender fileAppender = new FileAppender(path);
					fileAppender.open();
					
					appender = fileAppender;
				}else if(type.equalsIgnoreCase("all")){
					String path = config.getString(appenderName + ".path", logDir.getAbsolutePath() + File.separator + appenderName + ".log");
					FileAppender fileAppender = new FileAppender(path);
					
					AllAppender allAppender = new AllAppender();
					allAppender.addAppender(ConsoleAppender.INSTANCE);
					allAppender.addAppender(fileAppender);
					allAppender.open();
					
					appender = allAppender;
				}
				
				appenders.putIfAbsent(appenderName, appender);
				
				//对应的日志名
				String names = config.getString(appenderName+".logger", "");
				Iterable<String> it = Splitter.on(",").trimResults().omitEmptyStrings().split(names);
				for(String name : it){
					getConfiger(name).setAppender(appenders.get(appenderName));
				}
			}
		}
		
		//日志级别
		IConfig levelConfig = configFactory.getConfig("log.level");
		Set<String> levelKeys = levelConfig.keySet();
		for(String key: levelKeys){
			String names = levelConfig.getString(key, "");
			Iterable<String> it = Splitter.on(",").trimResults().omitEmptyStrings().split(names);
			for(String name : it){
				getConfiger(name).setLevel(levelMap.get(key.toLowerCase()));
			}
		}
		
	}
	
	public static LoggerConfiger getConfiger(String name){
		if(configers.containsKey(name)){
			return configers.get(name);
		}else{
			synchronized (LoggerConfiger.class) {
				if(configers.containsKey(name)){
					return configers.get(name);
				}
				LoggerConfiger configer = new LoggerConfiger(configers.get("ROOT"));
				configers.put(name, configer);
				return configer;				
			}
		}
	}
	
	public static void removeConfiger(String name) {
		LoggerConfiger configer = configers.remove(name);
		if(configer != null && configer.appender != null) {
			configer.appender.close();
		}
	}
	
	private LoggerConfiger(LoggerConfiger root) {
		this.appender = root.appender;
		this.level = root.level;
	}
	private LoggerConfiger() {}

	
	public IAppender getAppender() {
		return appender;
	}

	public void setAppender(IAppender appender) {		
		this.appender = appender;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	
}
