package com.github.wei.jtrace.agent;

import java.lang.reflect.Method;

/**
 * 执行代理类
 * @author wei-8
 */
public class AdvisorInvoker {


    // 钩子引用
    public static volatile Method ON_BEFORE_METHOD;

    public static void init(
            Method onBeforeMethod) {
    	ON_BEFORE_METHOD = onBeforeMethod;
    }

    
    public static void clean() {
        ON_BEFORE_METHOD = null;
    }

}
