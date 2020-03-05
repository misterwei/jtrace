package com.github.wei.jtrace.asm.util;

import com.github.wei.jtrace.api.transform.matcher.BaseClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.ExtractClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.InterfaceClassMatcher;
import com.github.wei.jtrace.asm.api.matcher.IMethodMatcher;
import com.github.wei.jtrace.asm.api.matcher.MethodArgumentMatcher;
import com.github.wei.jtrace.asm.api.matcher.MethodExtractMatcher;
import com.github.wei.jtrace.asm.api.matcher.MethodNameMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcherUtil {

    public static String classNameToPath(String name) {
        return name.replace('.', '/');
    }

    public static String[] classNameToPath(String[] names) {
        if(names != null) {
            for(int i=0;i<names.length;i++) {
                names[i] = names[i].replace('.', '/');
            }
            return names;
        }
        return null;
    }

    /**
     * 转换为ClassMatcher， className自动替换 '.' 为 '/'
     * @param className
     * @param matchType
     * @return
     * @throws Exception
     */
    public static IClassMatcher extractClassMatcher(String className, String matchType) throws Exception {
        className = classNameToPath(className);
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
        className = classNameToPath(className);
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

    public static List<IMethodMatcher> extractMethodMatchers(String methodStr) {
        List<IMethodMatcher> methodMatchers = null;
        if(methodStr != null) {
            String[] methods = methodStr.split(",");
            if(methods != null && methods.length> 0) {
                methodMatchers = new ArrayList<IMethodMatcher>(methods.length);
                for(int i=0;i<methods.length;i++) {
                    String m = methods[i].trim();
                    if(m.length() == 0){
                        continue;
                    }
                    methodMatchers.add(extractMethodMatcher(m));
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
