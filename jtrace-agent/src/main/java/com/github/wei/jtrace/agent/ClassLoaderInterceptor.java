package com.github.wei.jtrace.agent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行代理类
 * @author wei-8
 */
public class ClassLoaderInterceptor {


    // 钩子引用
    private static volatile ConcurrentHashMap<String, ClassLoader> CLASS_LOADERS = new ConcurrentHashMap<String, ClassLoader>();

    public static void init(
           ConcurrentHashMap<String, ClassLoader> classLoaders) {
    	ClassLoaderInterceptor.CLASS_LOADERS = classLoaders;
    }

    
    public static void clean() {
        CLASS_LOADERS = null;
    }

    public static Class<?> loadClass(String className){
        ClassLoader loader = CLASS_LOADERS.get(className);
        if(loader == null){
            return null;
        }
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
