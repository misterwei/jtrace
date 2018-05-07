package com.github.wei.jtrace.core.util;

public class StringUtil {

	/**
	 * 字符串匹配，^开头代表正则表达式
	 * @param pattern
	 * @param str
	 * @return
	 */
	public static boolean match(String pattern, String str) {
		if(pattern.startsWith("^")) {
			return str.matches(pattern);
		}
		return pattern.equals(str);
	}
	
	public static boolean equals(String str1, String str2) {
		return str1 == null? str2 == null : str1.equals(str2);
	}
	
	public static String replace(String str, char oldChar, char newChar) {
		if(str == null)
			return null;
		return str.replace(oldChar, newChar);
	}
}
