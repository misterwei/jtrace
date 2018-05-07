package com.github.wei.jtrace.core.service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.BeanRef;
import com.github.wei.jtrace.api.beans.IBeanDestroyProcessor;
import com.github.wei.jtrace.api.beans.IBeanFactory;
import com.github.wei.jtrace.api.beans.IBeanFactoryAware;
import com.github.wei.jtrace.api.beans.IBeanPostProcessor;
import com.github.wei.jtrace.api.beans.IBeanProcessorChain;
import com.github.wei.jtrace.api.beans.IProcessingBean;
import com.github.wei.jtrace.api.config.IConfig;
import com.github.wei.jtrace.api.config.IConfigFactory;
import com.github.wei.jtrace.api.exception.BeanProcessException;
import com.github.wei.jtrace.api.service.IAsyncService;
import com.github.wei.jtrace.api.service.IService;
import com.github.wei.jtrace.api.service.Stopable;
import com.github.wei.jtrace.core.util.Constants;

@Bean
public class ServiceManager implements IServiceManager, IBeanFactoryAware, IProcessingBean{
	private static final String CONFIG_PREFIX = "service.";
	private static final String ASYNC_POOL_NAME_PREFIX = "AsyncExecutor-";
	private static final int ASYNC_POOL_SIZE = 100;
	
	private ConcurrentHashMap<String, IService> services = new ConcurrentHashMap<String, IService>();	
	private ConcurrentHashMap<String, IAsyncService> asyncServices = new ConcurrentHashMap<String, IAsyncService>();	
	
	private IBeanPostProcessor beanPostProcessor = new ServiceBeanPostProcessor();
	private IBeanDestroyProcessor beanDestroyProcessor = new ServiceBeanDestroyProcessor();
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	
	private ExecutorService asyncExecutor = Executors.newFixedThreadPool(ASYNC_POOL_SIZE, new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, ASYNC_POOL_NAME_PREFIX + threadNumber.getAndIncrement());
			return t;
		}
	});
	
	@BeanRef(name="configFactory")
	private IConfigFactory configFactory;
	
	public void start(){
		Collection<IAsyncService> asyncValues =  asyncServices.values();
		for(IAsyncService service : asyncValues){
			IConfig config = configFactory.getConfig(CONFIG_PREFIX + service.getId());
			if(config.getBoolean(Constants.ENABLED, true)){
				if(!service.isRunning() && service.start(config)){
					asyncExecutor.execute(service);
				}
			}
		}
		
		Collection<IService> values =  services.values();
		for(IService service : values){
			IConfig config = configFactory.getConfig(CONFIG_PREFIX + service.getId());
			if(config.getBoolean(Constants.ENABLED, true)){
				service.start(config);
			}
		}
	}
	
	public boolean registAndStart(IService service){
		IConfig config = configFactory.getConfig(CONFIG_PREFIX + service.getId());
		if(config != null && !config.getBoolean(Constants.ENABLED, true)){
			return false;
		}
		
		if(service instanceof IAsyncService){
			asyncServices.putIfAbsent(service.getId(), (IAsyncService)service);
			if(service.start(config)){
				asyncExecutor.execute((IAsyncService)service);
				services.putIfAbsent(service.getId(), service);
				return true;
			}
			return false;
		}
		
		if(service.start(config)){
			services.putIfAbsent(service.getId(), service);
			return true;
		}
		return false;
	}
	
	public void stop(){
		Collection<IAsyncService> asyncValues =  asyncServices.values();
		for(IAsyncService service : asyncValues){
			if(service.isRunning()){
				service.stop();
			}
		}
		
		Collection<IService> values =  services.values();
		for(IService service : values){
			if(service instanceof Stopable){
				((Stopable)service).stop();
			}
		}
	}
	
	public void removeAndStop(String id){
		if(services.containsKey(id)){
			IService service = services.remove(id);
			if(service instanceof Stopable){
				((Stopable)service).stop();
			}
			return;
		}
		
		if(asyncServices.containsKey(id)) {
			IAsyncService asyncService = asyncServices.remove(id);
			if(asyncService.isRunning()) {
				asyncService.stop();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends IService> T getService(String id){
		return (T)services.get(id);
	}


	@Override
	public void setBeanFactory(IBeanFactory beanFactory) {
		beanFactory.registBeanPostProcessor(IService.class, beanPostProcessor);
		beanFactory.registBeanDestroyProcessor(IService.class, beanDestroyProcessor);
	}
	
	private class ServiceBeanPostProcessor implements IBeanPostProcessor{

		@Override
		public int priority() {
			return 0;
		}

		@Override
		public <T> T process(T obj, IBeanProcessorChain chain) throws BeanProcessException {
			if(obj instanceof IService){
				registAndStart((IService)obj);
			}
			return chain.doProcess(obj);
		}
		
	}
	
	private class ServiceBeanDestroyProcessor implements IBeanDestroyProcessor{

		@Override
		public int priority() {
			return 0;
		}

		@Override
		public <T> T process(T obj, IBeanProcessorChain chain) throws BeanProcessException {
			if(obj instanceof IService){
				removeAndStop(((IService)obj).getId());
			}
			return chain.doProcess(obj);
		}
		
	}

	@Override
	public void afterProcessComplete() {
		start();
	}
	

}
