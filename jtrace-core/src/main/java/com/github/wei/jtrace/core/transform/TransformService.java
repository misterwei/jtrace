package com.github.wei.jtrace.core.transform;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import com.github.wei.jtrace.api.service.IAsyncService;
import com.github.wei.jtrace.api.transform.ITransformer;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.core.util.AgentHelper;
import com.github.wei.jtrace.core.util.ClazzUtil;
import com.github.wei.jtrace.core.util.IdGenerator;

@Bean
public class TransformService implements IAsyncService{

	Logger log = LoggerFactory.getLogger("TransformService");
	
	public static final int ID_MAX_VALUE = Integer.MAX_VALUE;
	
	@BeanRef(name="instrumentation")
	private Instrumentation inst;
	
	@AutoRef
	private IClassFinderManager classFinderManager;
	
	private IdGenerator idGenerator = IdGenerator.generate(ID_MAX_VALUE);
	
	@Override
	public String getId() {
		return "transformer";
	}

	private LinkedBlockingQueue<IClassMatcher> matcherQueue = new LinkedBlockingQueue<IClassMatcher>();
		
	private ConcurrentHashMap<Integer, ITransformer > transformers = new ConcurrentHashMap<Integer, ITransformer >();
		
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
	public int registTransformer(ITransformer matcherAndResult, boolean refresh) throws IllegalAccessException {
		int id = idGenerator.next();
		
		if(!registTransformer(id, matcherAndResult, refresh)) {
			throw new IllegalAccessException("no ID sequence space");
		}
		
		return id;
	}

	/**
	 * @param id 
	 * @param matcherAndResult
	 * @param addToQueue 
	 * @return 如果id已经存在，返回false
	 */
	private boolean registTransformer(int id, ITransformer matcherAndResult, boolean refresh) {
		if(transformers.containsKey(id)) {
			return false;
		}
		transformers.put(id, matcherAndResult);
		if(refresh) {
			refreshTransformer(matcherAndResult);
		}
		return true;
	}
	
	public synchronized void removeTransformerByMatched(IClassDescriberTree classTree) throws ClassMatchException {
		Set<Integer> ids = transformers.keySet();
		Iterator<Integer> it = ids.iterator();
		while(it.hasNext()) {
			ITransformer matcher = transformers.get(it.next());
			if(matcher != null && matcher.matchClass(classTree)) {
				it.remove();
				refreshTransformer(matcher);
			}
		}
	}
	
	public void removeTransformerById(int id) {
		ITransformer transformer = transformers.remove(id);
		if(transformer != null) {
			refreshTransformer(transformer);
		}
	}
	
	public void removeTransformer(ITransformer transformer) {
		Set<Integer> ids = transformers.keySet();
		Iterator<Integer> it = ids.iterator();
		while(it.hasNext()) {
			ITransformer matcher = transformers.get(it.next());
			if(matcher != null && matcher.equals(transformer)) {
				it.remove();
				refreshTransformer(transformer);
			}
		}
	}
	
	//刷新Transformer，重新嵌码
	public boolean refreshTransformerById(int id) {
		ITransformer  transformer = transformers.get(id);
		if(transformer == null) {
			return false;
		}
		
		return refreshTransformer(transformer);
	}
	
	//刷新Transformer，重新嵌码
	public boolean refreshTransformer(IClassMatcher transformer) {
		if(transformer != null) {
			matcherQueue.add(transformer);
			return true;
		}
		return false;
	}
	
	//获取适配的Class
	public ITransformer getTransformer(int id){
		return transformers.get(id);
	}
	
	public List<Integer> getRegistedTransformerIds(){	
		Set<Integer> keys = transformers.keySet();
		List<Integer> result = new ArrayList<Integer>(keys);
		Collections.sort(result);
		return result;
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
				IClassMatcher matcher = matcherQueue.take();
				if(matcher == null) {
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
							if(matcher.matchClass(new ClassDescriberTreeFromClass(clazz))){
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
						log.warn("ClassMatcher {} not found matched classes", matcher);
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
			
			byte[] tempClassfileBuffer = classfileBuffer;
			
			try {
				ClassReader cr = new ClassReader(classfileBuffer);
				ClassDescriber descr = ClazzUtil.extractClassDescriber(cr);
				
				IClassFinder classFinder = classFinderManager.getClassFinder(loader);
				IClassDescriberTree classTree = new ClassDescriberTreeFromClassFinder(classFinder, descr);
				
				Set<Integer> ids = transformers.keySet();
				for(Integer id : ids) {
					final ITransformer transformer = transformers.get(id);
					if(transformer == null) {
						continue;
					}
					
					try {
						byte[] bytes  = transformer.transform(loader, classTree, classBeingRedefined, protectionDomain, tempClassfileBuffer);
						if(bytes != null) {
							tempClassfileBuffer = bytes;
						}
					}catch(Exception e) {
						log.error("Transform class ["+descr+"] failed", e);
					}
				}
			}catch(ClassFinderException e) {
				log.debug("Get classfinder for "+loader+" failed", e);
			}catch(Exception e) {
				log.error("Transform class ["+className+"] failed", e);
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
