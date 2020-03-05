package com.github.wei.jtrace.core.transform;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.wei.jtrace.api.transform.ITransformService;
import com.github.wei.jtrace.api.transform.ITransformer;
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
import com.github.wei.jtrace.api.transform.ITransformerMatcher;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.core.util.AgentHelper;
import com.github.wei.jtrace.core.util.ClazzUtil;
import com.github.wei.jtrace.core.util.IdGenerator;

@Bean(type=ITransformService.class)
public class TransformService implements ITransformService, IAsyncService{

	public static final int ID_MAX_VALUE = Integer.MAX_VALUE;

	private static Logger log = LoggerFactory.getLogger("TransformService");

	@BeanRef(name="instrumentation")
	private Instrumentation inst;
	
	@AutoRef
	private IClassFinderManager classFinderManager;
	
	private final IdGenerator idGenerator = IdGenerator.generate(ID_MAX_VALUE);

	private LinkedBlockingQueue<IClassMatcher> matcherQueue = new LinkedBlockingQueue<IClassMatcher>();

	private ConcurrentHashMap<Integer, ITransformerMatcher> transformers = new ConcurrentHashMap<Integer, ITransformerMatcher>();

	private boolean classout = false;
	private volatile boolean running = false;

	private ThreadLocal<IClassDescriberTree> cachedClassDecriberTree = new ThreadLocal<IClassDescriberTree>();

	@Override
	public String getId() {
		return "transformer";
	}

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
	 * @param transformerMatcher
	 * @param refresh
	 * @return
	 * @throws IllegalAccessException 
	 */
	public int registerTransformerMatcher(ITransformerMatcher transformerMatcher, boolean refresh) throws IllegalAccessException {
		int id = idGenerator.next();
		
		if(!registerTransformerMatcher(id, transformerMatcher, refresh)) {
			throw new IllegalAccessException("no ID sequence space");
		}
		
		return id;
	}

	/**
	 * 立即执行
	 * @param transformerMatcher
	 * @return
	 * @throws IllegalAccessException
	 */
	@Override
	public int registerTransformerMatcherImmediately(ITransformerMatcher transformerMatcher) throws IllegalAccessException {
		int id = idGenerator.next();

		if(!registerTransformerMatcher(id, transformerMatcher, false)) {
			throw new IllegalAccessException("no ID sequence space");
		}

		matchAndRestransform(transformerMatcher);

		return id;
	}

	/**
	 * @param id 
	 * @param matcherAndResult
	 * @param refresh
	 * @return 如果id已经存在，返回false
	 */
	private boolean registerTransformerMatcher(int id, ITransformerMatcher matcherAndResult, boolean refresh) {
		if(transformers.containsKey(id)) {
			return false;
		}
		transformers.put(id, matcherAndResult);
		if(refresh) {
			refreshTransformer(matcherAndResult);
		}
		return true;
	}
	
	public void removeTransformerMatcherByMatched(IClassDescriberTree classTree) throws ClassMatchException {
		Set<Map.Entry<Integer, ITransformerMatcher>> entries = transformers.entrySet();
		Iterator<Map.Entry<Integer, ITransformerMatcher>> it = entries.iterator();
		while(it.hasNext()) {
			ITransformerMatcher matcher = it.next().getValue();
			if(matcher.matchClass(classTree)) {
				it.remove();
				refreshTransformer(matcher);
			}
		}
	}
	
	public void removeTransformerMatcherById(int id) {
		ITransformerMatcher transformer = transformers.remove(id);
		if(transformer != null) {
			refreshTransformer(transformer);
		}
	}
	
	public void removeTransformerMatcher(ITransformerMatcher transformer) {
		Set<Integer> ids = transformers.keySet();
		Iterator<Integer> it = ids.iterator();
		while(it.hasNext()) {
			ITransformerMatcher matcher = transformers.get(it.next());
			if(matcher != null && matcher.equals(transformer)) {
				it.remove();
				refreshTransformer(transformer);
			}
		}
	}
	
	//刷新Transformer，重新嵌码
	public boolean refreshTransformerById(int id) {
		ITransformerMatcher transformer = transformers.get(id);
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
	public ITransformerMatcher getTransformerMatcherById(int id){
		return transformers.get(id);
	}
	
	public List<Integer> getRegisteredTransformerMatcherIds(){
		Set<Integer> keys = transformers.keySet();
		List<Integer> result = new ArrayList<Integer>(keys);
		Collections.sort(result);
		return result;
	}
	
	public IClassDescriberTree findClassDescriberTree(String className){
		try {
			IClassFinder classFinder = classFinderManager.getClassFinder(null);
			return new ClassDescriberTreeFromClassFinder(classFinder, ClazzUtil.classNameToPath(className));
		}catch(ClassFinderException e){
			log.error("Failed to get class finder for " + className, e);
		}catch(Exception e) {
			log.error("Failed to find class describer tree for "+className, e);
		}
		return null;
	}

	private void matchAndRestransform(IClassMatcher matcher){
		Class<?>[] classes = inst.getAllLoadedClasses();
		if(classes != null){
			boolean found = false;
			for(Class<?> clazz : classes){
				if(clazz == null) {
					continue;
				}
				if(ClazzUtil.isJtraceClass(clazz.getClassLoader(), clazz.getName())) {
					continue;
				}
				boolean inner_found = false;
				try {
					IClassDescriberTree classTree = new ClassDescriberTreeFromClass(clazz);
					if(matcher.matchClass(classTree)){
						found = true;
						inner_found = true;
						cachedClassDecriberTree.set(classTree);
						if(inst.isModifiableClass(clazz)) {
							inst.retransformClasses(clazz);
						}else {
							log.warn("This class [{}] does not support rewriting", clazz);
						}
					}
				}catch(UnmodifiableClassException ex){
					log.error("Re transform class ["+clazz+"] failed", ex);
				}catch(ClassMatchException e) {
					log.error("Match class ["+clazz+"] failed", e);
				}catch(Throwable e){
					log.error("Re transform class ["+clazz+"] failed", e);
				}finally {
					if(inner_found) {
						cachedClassDecriberTree.remove();
					}
				}
			}

			if(!found) {
				log.warn("ClassMatcher {} not found matched classes", matcher);
			}
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

				matchAndRestransform(matcher);
			}
			log.info("Matcher task is stopped");
		} catch (InterruptedException e) {
			log.warn("Matcher task is stopped", e);
		}
		running = false;
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
				//如果从Retransform过来的则直接使用缓存的ClassDescriberTree
				IClassDescriberTree classTree = TransformService.this.cachedClassDecriberTree.get();
				if(classTree == null){
					ClassReader cr = new ClassReader(classfileBuffer);
					ClassDescriber describer = ClazzUtil.extractClassDescriber(cr);
					IClassFinder classFinder = classFinderManager.getClassFinder(loader);
				 	classTree = new ClassDescriberTreeFromClassFinder(classFinder, describer);
				}

				Set<Map.Entry<Integer, ITransformerMatcher>> entries = transformers.entrySet();
				for(Map.Entry<Integer, ITransformerMatcher> entry: entries) {
					ITransformerMatcher transformerMatcher = entry.getValue();
					ITransformer transformer = transformerMatcher.matchedTransformer(classTree);
					if(transformer == null) {
						continue;
					}

					byte[] bytes = transformer.transform(loader, classTree, classBeingRedefined, protectionDomain, tempClassfileBuffer);
					if (bytes != null) {
						tempClassfileBuffer = bytes;
					}
				}
			}catch (ClassMatchException e){
				log.warn("Failed to get matched transformer for " + className, e);
			}catch(ClassFinderException e) {
				log.warn("Failed to get class finder for " + className, e);
			}catch(Exception e) {
				log.error("Failed to transform class " + className, e);
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
