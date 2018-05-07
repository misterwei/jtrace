package com.github.wei.jtrace.core.matchers;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.BeanRef;
import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.clazz.IClassFinder;
import com.github.wei.jtrace.api.clazz.IClassFinderManager;
import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.exception.ClassFinderException;
import com.github.wei.jtrace.api.exception.ClassMatchException;
import com.github.wei.jtrace.api.matcher.IMatcherAndTransformer;
import com.github.wei.jtrace.api.service.IAsyncService;
import com.github.wei.jtrace.core.util.AgentHelper;
import com.github.wei.jtrace.core.util.ClazzUtil;
import com.github.wei.jtrace.core.util.IdGenerator;

@Bean
public class MatchAndTransformService implements IAsyncService{

	Logger log = LoggerFactory.getLogger("MatchAndTransformService");
	
	public static final int ID_MAX_VALUE = 1000;
	
	@BeanRef(name="instrumentation")
	private Instrumentation inst;
	
	@AutoRef
	private IClassFinderManager classFinderManager;
	
	private IdGenerator idGenerator = IdGenerator.generate(ID_MAX_VALUE);
	
	@Override
	public String getId() {
		return "transformer";
	}

	private LinkedBlockingQueue<Integer> matcherQueue = new LinkedBlockingQueue<Integer>();
		
	private Map<Integer, IMatcherAndTransformer > matchedClassesAndResult = Collections.synchronizedMap(new HashMap<Integer, IMatcherAndTransformer >());
		
	private boolean classout = false;
	private volatile boolean running = false;
	
	@Override
	public boolean start(IConfig config) {
		inst.addTransformer(weaveTransformer, true);
		classout = config.getBoolean("classout", false);
		return running = true;
		
	}

	@Override
	public void stop() {
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	/**
	 * 注册IMatcherAndTransformer
	 * @param matcherAndResult
	 * @param addToQueue 
	 * @return
	 * @throws IllegalAccessException 
	 */
	public synchronized int registTransformer(IMatcherAndTransformer matcherAndResult, boolean refresh) throws IllegalAccessException {
		int id = idGenerator.next();
		
		int i = 0;
		while(!registTransformer(id, matcherAndResult, refresh)) {
			i++;
			if(i >= ID_MAX_VALUE) {
				throw new IllegalAccessException("no ID sequence space");
			}
		}
		
		return id;
	}

	/**
	 * @param id 
	 * @param matcherAndResult
	 * @param addToQueue 
	 * @return 如果id已经存在，返回false
	 */
	private boolean registTransformer(int id, IMatcherAndTransformer matcherAndResult, boolean refresh) {
		if(matchedClassesAndResult.containsKey(id)) {
			return false;
		}
		matchedClassesAndResult.put(id, matcherAndResult);
		if(refresh) {
			matcherQueue.add(id);
		}
		return true;
	}
	
	public synchronized void removeTransformerByMatched(IClassDescriberTree classTree) {
		Set<Integer> ids = matchedClassesAndResult.keySet();
		Iterator<Integer> it = ids.iterator();
		while(it.hasNext()) {
			IMatcherAndTransformer matcher = matchedClassesAndResult.get(it.next());
			try {
				if(matcher.matchClass(classTree)) {
					it.remove();
				}
			}catch(ClassMatchException e) {
				log.debug("Match class ["+classTree.getClassDescriber()+"] failed", e);
			}
		}
		
	}
	
	public synchronized void removeTransformerById(int id) {
		matchedClassesAndResult.remove(id);
	}
	
	
	//刷新ClassMatcher，重新适配
	public synchronized boolean refreshTransformer(int id) {
		if(matchedClassesAndResult.containsKey(id)) {
			matcherQueue.add(id);
			return true;
		}
		return false;
	}
	
	//获取适配的Class
	public IMatcherAndTransformer getTransformer(int id){
		return matchedClassesAndResult.get(id);
	}
	
	public IClassDescriberTree findClassDescriberTree(String className){
		try {
			IClassFinder classFinder = classFinderManager.getClassFinder(null);
			return new ClassDescriberTreeFromClassFinder(classFinder, ClazzUtil.classNameToPath(className));
		}catch(Exception e) {
			log.warn("MatcherAndTransformer getClassDescriberTree ["+className+"] failed", e);
			return null;
		}
	}
	
	@Override
	public void run() {
		try {
			while(running){
				Integer matcherAndTransformerId = matcherQueue.take();
				IMatcherAndTransformer matcherAndTransformer = matchedClassesAndResult.get(matcherAndTransformerId);
				if(matcherAndTransformer == null) {
					continue;
				}
				
				Class<?>[] clazzs = inst.getAllLoadedClasses();
				if(clazzs != null){
					boolean found = false;
					for(Class<?> clazz : clazzs){
						if(clazz == null) {
							continue;
						}
						if(ClazzUtil.isJtraceClass(clazz.getClassLoader(), clazz.getName())) {
							continue;
						}
						try {
							if(matcherAndTransformer.matchClass(new ClassDescriberTreeFromClass(clazz))){
								found = true;
								try {
									if(inst.isModifiableClass(clazz)) {
										inst.retransformClasses(clazz);
									}else {
										log.warn("This class [{}] does not support rewriting", clazz);
									}
								} catch (Exception e) {
									if(log.isWarnEnabled()) {
										log.warn("Retransform class "+clazz+" failed", e);
									}
								}

							}
						}catch(Exception e) {
							log.error("MatcherAndTransformer match class ["+clazz+"] failed", e);
						}
					}
					
					if(!found) {
						//delayClassMatcherTimer.addMatcherAndTransformer(matcherAndTransformer);
						log.warn("MatcherAndTransformer {} not found matched classes", matcherAndTransformerId);
					}
				}
			}
		} catch (InterruptedException e) {
			if(log.isWarnEnabled()) {
				log.warn("MatcherService exception", e);
			}
		}
		
		running = false;
		log.info("MatcherService stoped");
	}
	
	//负责嵌码
	private ClassFileTransformer weaveTransformer = new ClassFileTransformer() {
		
		@Override
		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
			if(ClazzUtil.isJtraceClass(loader, className)) {
				return null;
			}
			
			ClassReader cr = new ClassReader(classfileBuffer);
			ClassDescriber descr = ClazzUtil.extractClassDescriber(cr);
			
			byte[] tempClassfileBuffer = classfileBuffer;
			Set<Integer> ids = matchedClassesAndResult.keySet();
			for(Integer id : ids) {
				final IMatcherAndTransformer matcherAndResult = matchedClassesAndResult.get(id);
				if(matcherAndResult == null) {
					continue;
				}
				
				try {
					IClassFinder classFinder = classFinderManager.getClassFinder(loader);
					if(matcherAndResult.matchClass(new ClassDescriberTreeFromClassFinder(classFinder, descr))) {
						byte[] bytes  = matcherAndResult.transform(loader, className, classBeingRedefined, protectionDomain, tempClassfileBuffer);
						if(bytes != null) {
							tempClassfileBuffer = bytes;
						}
					}
				}catch(ClassMatchException e) {
					log.debug("Match class ["+descr+"] failed", e);
				}catch(ClassFinderException e) {
					log.debug("Get classfinder for "+loader+" failed", e);
				}catch(Exception e) {
					log.error("Match transform class ["+descr+"] failed", e);
				}
			}
			
			if(tempClassfileBuffer != classfileBuffer) {
				if(classout) {
					try {
						File file = AgentHelper.getAgentDirectory();
						File classoutPath = new File(file, "classout");
						String[] classPathAndFile = ClazzUtil.splitClassPathAndFile(className);
						String classFileName = null;
						if(classPathAndFile != null) {
							classoutPath = new File(classoutPath, classPathAndFile[0]);
							classFileName = classPathAndFile[1];
						}else {
							classFileName = className;
						}
						if(!classoutPath.exists()) {
							classoutPath.mkdirs();
						}
						File classFile = new File(classoutPath, classFileName+".class");
						FileOutputStream out = new FileOutputStream(classFile);
						out.write(tempClassfileBuffer);
						out.flush();
						out.close();
					}catch(Exception e) {
						log.warn("writing class out failed " + className,e);
					}
				}
				return tempClassfileBuffer;
			}
			
			return null;
		}
	
	};
	
}
