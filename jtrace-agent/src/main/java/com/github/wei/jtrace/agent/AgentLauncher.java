package com.github.wei.jtrace.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

/**
 * 
 * @author MM-Baby
 */
public class AgentLauncher {
	private static final String AGENT_CORE_JAR = "jtrace-core.jar";
    private static volatile ClassLoader jtraceClassLoader;

    public static void premain(String args, Instrumentation inst) {
        main(args, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        main(args, inst);
    }


    public synchronized static void resetGreysClassLoader() {
    	jtraceClassLoader = null;
    }

    private static ClassLoader loadOrDefineClassLoader(URL agentJar) throws Throwable {

        if (null != jtraceClassLoader) {
            return jtraceClassLoader;
        }
        jtraceClassLoader = new AgentClassLoader(agentJar);

        return jtraceClassLoader;
    }

    private static synchronized void main(final String args, final Instrumentation inst) {
        try {
        	URL agent_url = AgentLauncher.class.getProtectionDomain().getCodeSource().getLocation();
            
            System.out.println(agent_url);
            
            URL url = new URL(agent_url, AGENT_CORE_JAR);
            System.out.println(url);

            JarFile jarfile = new JarFile(agent_url.getFile());
            inst.appendToBootstrapClassLoaderSearch(
                    jarfile
            );
            
            inst.appendToSystemClassLoaderSearch(jarfile);
            
            final ClassLoader agentLoader = loadOrDefineClassLoader(url);
            Class<?> starterClass = agentLoader.loadClass("com.github.wei.jtrace.core.JtraceLauncher");
            Method starterMethod = starterClass.getMethod("start", String[].class, Instrumentation.class);
            Field exportClassField = starterClass.getField("EXPORT_CLASS");

            ConcurrentHashMap<String, ClassLoader> exportClassMap =
                    (ConcurrentHashMap<String, ClassLoader>)exportClassField.get(null);
            ClassLoaderInterceptor.init(exportClassMap);

            Object starter = starterClass.newInstance();
            starterMethod.invoke(starter, null, inst);
            
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
}
