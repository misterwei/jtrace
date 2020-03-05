package com.github.wei.jtrace.core.transform;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.BeanRef;
import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.service.IAsyncService;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.core.util.ClazzUtil;

@Bean
public class MatchAndRestoreService implements IAsyncService{

	Logger log = LoggerFactory.getLogger(MatchAndRestoreService.class);
	
	@BeanRef(name="instrumentation")
	private Instrumentation inst;
	
	@AutoRef
	private TransformService matchAndTransformService;
	
	private LinkedBlockingQueue<IClassMatcher> matcherQueue = new LinkedBlockingQueue<IClassMatcher>();

	
	@Override
	public String getId() {
		return "restore";
	}

	private volatile boolean running = false;
	
	@Override
	public boolean start(IConfig config) {
		return running = true;
	}

	@Override
	public void stop() {
		running = false;
		
	}
	
	public  void restoreByMatched(IClassMatcher matcher) {
		matcherQueue.add(matcher);
	}
	
	@Override
	public void run() {
		try {
			while(running){
				IClassMatcher matcher = matcherQueue.take();
				
				
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
							IClassDescriberTree classTree = new ClassDescriberTreeFromClass(clazz);
							if(matcher.matchClass(classTree)){
								found = true;
								
								//必须先清除TransformService中的MatcherAndTransfomer
								matchAndTransformService.removeTransformerMatcherByMatched(classTree);
								
								try {
									if(inst.isModifiableClass(clazz)) {
										inst.retransformClasses(clazz);
									}else {
										log.warn("This class [{}] does not support rewriting", clazz);
									}
								} catch (Exception e) {
									if(log.isWarnEnabled()) {
										log.warn("Restore class "+clazz+" failed", e);
									}
								}

							}
						}catch(Exception e) {
							log.error("Restore match class ["+clazz+"] failed", e);
						}
					}
					
					if(!found) {
						log.warn("Restore {} not found matched classes", matcher);
					}
				}
			}
		} catch (InterruptedException e) {
			if(log.isWarnEnabled()) {
				log.warn("Restore exception", e);
			}
		}
		
		running = false;
		log.info("RestoreService stoped");
	}

	@Override
	public boolean isRunning() {
		return running;
	}
	
	//负责恢复
//	private ClassFileTransformer restoreTransformer = new ClassFileTransformer() {
//		
//		@Override
//		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
//				ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
//			return null;
//		}
//	
//	};
//	
}
