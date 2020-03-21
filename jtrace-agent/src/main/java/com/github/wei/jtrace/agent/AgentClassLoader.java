package com.github.wei.jtrace.agent;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class AgentClassLoader extends URLClassLoader {


    public AgentClassLoader(URL agentJarUrl) throws MalformedURLException {
        super(new URL[]{agentJarUrl}, null);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        try {
            Class<?> aClass = findClass(name);
            if (resolve) {
                resolveClass(aClass);
            }
            return aClass;
        } catch (Exception e) {
            return super.loadClass(name, resolve);
        }
    }

}
