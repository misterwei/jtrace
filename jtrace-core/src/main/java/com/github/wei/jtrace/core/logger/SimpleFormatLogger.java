package com.github.wei.jtrace.core.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

public class SimpleFormatLogger implements org.slf4j.Logger {
	private String name;
	private LoggerConfiger configer;
	private final String formater = "%s [%s] [%s] %s - %s"; //时间 [线程] [name] Level - 日志
	
	public SimpleFormatLogger(String name) {
		this.name = name;
		this.configer = LoggerConfiger.getConfiger(name);
	}
	
	@Override
	public String getName() {
		return name;
	}

	private void writeLog(String level, String message, Object... arguments){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formated  = null;
		if(arguments != null && arguments.length > 0){
			formated = MessageFormatter.arrayFormat(message, arguments).getMessage();
			formated = String.format(formater, dateFormat.format(new Date()), Thread.currentThread().getName(), getName(), level, formated);
		}else{
			formated = String.format(formater, dateFormat.format(new Date()), Thread.currentThread().getName(), getName(), level, message);
		}
		
		configer.getAppender().append(formated);
	}
	
	private void writeLog(String level, String message, Throwable thr){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		StringWriter out = new StringWriter();
		PrintWriter writer = new PrintWriter(out);
		thr.printStackTrace(writer);
		
		String throwMessage = out.toString();
		
		String formated = String.format(formater, dateFormat.format(new Date()), Thread.currentThread().getName(), getName(), level, message);
		configer.getAppender().append(formated);
		configer.getAppender().append(throwMessage);
	}
	
	@Override
	public boolean isTraceEnabled() {
		return LoggerFlags.LEVEL_TRACE >= configer.getLevel();
	}

	@Override
	public void trace(String msg) {
		if(!isTraceEnabled()){
			return;
		}
		writeLog("TRACE", msg);
	}

	@Override
	public void trace(String format, Object arg) {
		if(!isTraceEnabled()){
			return;
		}
		writeLog("TRACE", format, arg);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		if(!isTraceEnabled()){
			return;
		}
		writeLog("TRACE", format, arg1, arg2);
	}

	@Override
	public void trace(String format, Object... arguments) {
		if(!isTraceEnabled()){
			return;
		}
		writeLog("TRACE", format, arguments);
	}

	@Override
	public void trace(String msg, Throwable t) {
		if(!isTraceEnabled()){
			return;
		}
		writeLog("TRACE", msg, t);
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return false;
	}

	@Override
	public void trace(Marker marker, String msg) {}

	@Override
	public void trace(Marker marker, String format, Object arg) {}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {}

	@Override
	public void trace(Marker marker, String format, Object... argArray) {}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {}

	@Override
	public boolean isDebugEnabled() {
		return LoggerFlags.LEVEL_DEBUG >= configer.getLevel();
	}

	@Override
	public void debug(String msg) {
		if(!isDebugEnabled()){
			return;
		}
		writeLog("DEBUG", msg);
	}

	@Override
	public void debug(String format, Object arg) {
		if(!isDebugEnabled()){
			return;
		}
		writeLog("DEBUG", format, arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		if(!isDebugEnabled()){
			return;
		}
		writeLog("DEBUG", format, arg1, arg2);
	}

	@Override
	public void debug(String format, Object... arguments) {
		if(!isDebugEnabled()){
			return;
		}
		writeLog("DEBUG", format, arguments);
	}

	@Override
	public void debug(String msg, Throwable t) {
		if(!isDebugEnabled()){
			return;
		}
		writeLog("DEBUG", msg, t);
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return false;
	}

	@Override
	public void debug(Marker marker, String msg) {}

	@Override
	public void debug(Marker marker, String format, Object arg) {}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {}

	@Override
	public void debug(Marker marker, String format, Object... arguments) {}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {}

	@Override
	public boolean isInfoEnabled() {
		return LoggerFlags.LEVEL_INFO >= configer.getLevel();
	}

	@Override
	public void info(String msg) {
		if(!isInfoEnabled()){
			return;
		}
		writeLog("INFO", msg);
	}

	@Override
	public void info(String format, Object arg) {
		if(!isInfoEnabled()){
			return;
		}
		writeLog("INFO", format, arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		if(!isInfoEnabled()){
			return;
		}
		writeLog("INFO", format, arg1, arg2);
	}

	@Override
	public void info(String format, Object... arguments) {
		if(!isInfoEnabled()){
			return;
		}
		writeLog("INFO", format, arguments);
	}

	@Override
	public void info(String msg, Throwable t) {
		if(!isInfoEnabled()){
			return;
		}
		writeLog("INFO", msg, t);
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return false;
	}

	@Override
	public void info(Marker marker, String msg) {}

	@Override
	public void info(Marker marker, String format, Object arg) {}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {}

	@Override
	public void info(Marker marker, String format, Object... arguments) {}

	@Override
	public void info(Marker marker, String msg, Throwable t) {}

	@Override
	public boolean isWarnEnabled() {
		return LoggerFlags.LEVEL_WARN >= configer.getLevel();
	}

	@Override
	public void warn(String msg) {
		if(!isWarnEnabled()){
			return;
		}
		writeLog("WARN", msg);
	}

	@Override
	public void warn(String format, Object arg) {
		if(!isWarnEnabled()){
			return;
		}
		writeLog("WARN", format, arg);
	}

	@Override
	public void warn(String format, Object... arguments) {
		if(!isWarnEnabled()){
			return;
		}
		writeLog("WARN", format, arguments);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		if(!isWarnEnabled()){
			return;
		}
		writeLog("WARN", format, arg1, arg2);
	}

	@Override
	public void warn(String msg, Throwable t) {
		if(!isWarnEnabled()){
			return;
		}
		writeLog("WARN", msg, t);
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return false;
	}

	@Override
	public void warn(Marker marker, String msg) {}

	@Override
	public void warn(Marker marker, String format, Object arg) {}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {}

	@Override
	public void warn(Marker marker, String format, Object... arguments) {}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {}

	@Override
	public boolean isErrorEnabled() {
		return LoggerFlags.LEVEL_ERROR >= configer.getLevel();
	}

	@Override
	public void error(String msg) {
		if(!isErrorEnabled()){
			return;
		}
		writeLog("ERROR", msg);
	}

	@Override
	public void error(String format, Object arg) {
		if(!isErrorEnabled()){
			return;
		}
		writeLog("ERROR", format, arg);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		if(!isErrorEnabled()){
			return;
		}
		writeLog("ERROR", format, arg1, arg2);
	}

	@Override
	public void error(String format, Object... arguments) {
		if(!isErrorEnabled()){
			return;
		}
		writeLog("ERROR", format, arguments);
	}

	@Override
	public void error(String msg, Throwable t) {
		if(!isErrorEnabled()){
			return;
		}
		writeLog("ERROR", msg, t);
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return false;
	}

	@Override
	public void error(Marker marker, String msg) {}

	@Override
	public void error(Marker marker, String format, Object arg) {}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {}

	@Override
	public void error(Marker marker, String format, Object... arguments) {}

	@Override
	public void error(Marker marker, String msg, Throwable t) {}

}
