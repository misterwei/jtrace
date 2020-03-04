package com.github.wei.jtrace.core.service;

import java.util.*;
import java.util.concurrent.*;
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
import com.github.wei.jtrace.api.exception.ServiceStartException;
import com.github.wei.jtrace.api.exception.ServiceStopException;
import com.github.wei.jtrace.api.service.*;
import com.github.wei.jtrace.core.exception.ServiceAlreadyExistsException;
import com.github.wei.jtrace.core.util.Constants;

@Bean
public class ServiceManager implements IServiceManager, IBeanFactoryAware, IProcessingBean{
	private static final String CONFIG_PREFIX = "service.";
	private static final String ASYNC_POOL_NAME_PREFIX = "AsyncExecutor-";
	private static final int ASYNC_POOL_SIZE = 100;
	
	private ConcurrentHashMap<String, IService> services = new ConcurrentHashMap<String, IService>();
	private CopyOnWriteArrayList<IService> orderedServices = new CopyOnWriteArrayList<IService>();

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
	
	public void start() throws ServiceStartException {
		List<IAsyncService> asyncServiceList = new ArrayList<IAsyncService>();
		for(IService service : orderedServices){
			IConfig config = configFactory.getConfig(CONFIG_PREFIX + service.getId());
			if(config.getBoolean(Constants.ENABLED, true)
					&& service.start(config)){
				if(service instanceof IAsyncService){
					asyncServiceList.add((IAsyncService)service);
				}
			}
		}

		for(IAsyncService service : asyncServiceList){
			asyncExecutor.execute(service);
		}
	}
	
	public boolean addAndStart(IService service) throws ServiceAlreadyExistsException, ServiceStartException{
		IConfig config = configFactory.getConfig(CONFIG_PREFIX + service.getId());
		if(config != null && !config.getBoolean(Constants.ENABLED, true)){
			return false;
		}

		IService old = services.putIfAbsent(service.getId(), service);
		if(old != null){
			throw new ServiceAlreadyExistsException(old.getId());
		}

		orderedServices.add(service);

		if(!service.start(config)){
			return false;
		}

		if(service instanceof IAsyncService){
			asyncExecutor.execute((IAsyncService)service);
		}

		return true;
	}
	
	public void stop() throws ServiceStopException {

		for(IService service : orderedServices){
			if(service instanceof Stoppable){
				((Stoppable)service).stop();
			}
		}
	}
	
	public void removeAndStop(String id) throws ServiceStopException{
		if(services.containsKey(id)){
			IService service = services.remove(id);
			if(service instanceof Stoppable){
				((Stoppable)service).stop();
			}
			orderedServices.remove(service);
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
				try {
					addAndStart((IService)obj);
				} catch (ServiceAlreadyExistsException e) {
					throw new BeanProcessException(e);
				} catch (ServiceStartException e) {
					throw new BeanProcessException(e);
				}
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
				try {
					removeAndStop(((IService)obj).getId());
				} catch (ServiceStopException e) {
					throw new BeanProcessException(e);
				}
			}
			return chain.doProcess(obj);
		}
		
	}

	@Override
	public void afterProcessComplete() throws BeanProcessException {
		try {
			start();
		} catch (ServiceStartException e) {
			throw new BeanProcessException(e);
		}
	}
	

}
