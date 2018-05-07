package com.github.wei.jtrace.core.beans;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.IBeanDestroyProcessor;
import com.github.wei.jtrace.api.beans.IBeanFactory;
import com.github.wei.jtrace.api.beans.IBeanPostProcessor;
import com.github.wei.jtrace.api.beans.IBeanProcessor;
import com.github.wei.jtrace.api.beans.IBeanProcessorChain;
import com.github.wei.jtrace.api.beans.IProcessingBean;
import com.github.wei.jtrace.api.exception.BeanCheckException;
import com.github.wei.jtrace.api.exception.BeanInstantiationException;
import com.github.wei.jtrace.api.exception.BeanNotFoundException;
import com.github.wei.jtrace.api.exception.BeanProcessException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class DefaultBeanFactory implements IBeanFactory{
	Logger log = LoggerFactory.getLogger("DefaultBeanFactory");
	private Cache<String, Object> cache = CacheBuilder.newBuilder().build() ;
	
	private ConcurrentHashMap<String, ILazyBeanCreator<?> > callbacks = new ConcurrentHashMap<String, ILazyBeanCreator<?> >();
	private ConcurrentHashMap<Class<?>, String > beanTypes = new ConcurrentHashMap<Class<?>, String >();

	private List<DefaultBeanProcessorChain> beanPostProcessors = new CopyOnWriteArrayList<DefaultBeanProcessorChain>();
	private List<DefaultBeanProcessorChain> beanDestroyProcessors = new CopyOnWriteArrayList<DefaultBeanProcessorChain>();

	private AutoInjectProcessorChain autoInjectProcessor = new AutoInjectProcessorChain(this);
	
	public DefaultBeanFactory() {
		DefaultBeanProcessorChain chain = getOrCreateProcessorChain(null, beanPostProcessors);
		addToProcessorChain(new BeanFactoryInjectProcessor(this), chain);
	}
	

	@Override
	public <T> T getBean(final Class<T> beanClass) throws ExecutionException {
		final String beanName = beanTypes.get(beanClass);
		if(beanName == null){
			throw new ExecutionException(new BeanNotFoundException(beanClass.getName()));
		}
		
		return getBean(beanName);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean(final String beanName) throws ExecutionException{
	
		return (T)cache.get(beanName, new Callable<T>() {
			@Override
			public T call() throws Exception {
				ILazyBeanCreator<?> call = callbacks.remove(beanName);
				if(call != null){
					Object obj = call.create();
					if(obj == null){
						throw new BeanNotFoundException(beanName);
					}
					return (T)obj;
				}
				throw new BeanNotFoundException(beanName);
			}
		});
		
	}

	@Override
	public boolean containsBean(String beanName) {
		return cache.getIfPresent(beanName) != null || callbacks.contains(beanName);
	}

	@Override
	public <T> T registBean(final Class<T> clazz) throws BeanInstantiationException, BeanCheckException, BeanProcessException {
		Bean bean = checkAndGetBean(clazz);
		
		String beanName = extractBeanName(bean, clazz);
		
		checkBeanExist(beanName, clazz);
		log.debug("Regist bean {} for {}", beanName, clazz);
		
		Class<?>[] beanTypesRef = bean.type();
		checkAndLinkBeanTypes(clazz, beanName, beanTypesRef);
		
		T obj = newBeanInstance(clazz);
		
		cache.put(beanName, obj);
		
		return obj;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void registLazyBean(final Class<?> clazz) throws BeanCheckException {
		Bean bean = checkAndGetBean(clazz);
		
		String beanName = extractBeanName(bean, clazz);
		checkBeanExist(beanName, clazz);
		log.debug("Regist lazy bean {} for {}", beanName, clazz);
		
		Class<?>[] beanTypesRef = bean.type();
		checkAndLinkBeanTypes(clazz, beanName, beanTypesRef);
	
		callbacks.put(beanName, new ILazyBeanCreator() {
			@Override
			public Object create() throws Exception {
				return newBeanInstance(clazz);
			}
		});
	}
	
	@Override
	public <T> T registObject(String beanName, T obj) throws BeanCheckException, BeanProcessException{
		checkBeanExist(beanName, obj.getClass());
		log.debug("Regist object bean {} for {}", beanName, obj);
		
		T bean = doBeanPostProcess( obj);
		
		cache.put(beanName, bean);
		return (T)bean;
	}
	
	private String extractBeanName(Bean bean, Class<?> clazz){
		if(!"".equals(bean.name())){
			return bean.name();
		}
		return clazz.getName();
	}
	
	private <T> T newBeanInstance(final Class<T> clazz) throws BeanInstantiationException, BeanProcessException{
		T obj;
		try {
			obj = clazz.newInstance();
		} catch (InstantiationException e) {
			throw new BeanInstantiationException(clazz, e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new BeanInstantiationException(clazz, e.getMessage(), e);
		}

		try {
			//自动注入
			obj = cloneProcessorChain(autoInjectProcessor).doProcess(obj);
			
			//后处理
			obj = doBeanPostProcess(obj);
			
			return obj;
		}catch(Exception e) {
			throw new BeanInstantiationException(clazz, e.getMessage(), e);
		}
	}
	
	private void checkAndLinkBeanTypes(Class<?> beanClass, String beanName, Class<?>[] beanTypesRef) throws BeanCheckException{
		for(Class<?> beanType : beanTypesRef){
			if(beanType != Object.class){
				checkBeanType(beanType, beanClass);
				beanTypes.put(beanType, beanName);
			}
		}
		
		//将自己也加入beanTypes
		beanTypes.put(beanClass, beanName);
	}
	
	private Bean checkAndGetBean(Class<?> clazz) throws BeanCheckException{
		Bean bean = clazz.getAnnotation(Bean.class);
		if(bean == null){
			throw new BeanCheckException(clazz, "This class does not annotate @Bean");
		}
		return bean;
	}
	
	private void checkBeanExist(String beanName, Class<?> clazz) throws BeanCheckException{
		if(containsBean(beanName)){
			throw new BeanCheckException(clazz, "this bean ["+beanName+"] has already existed");
		}
	}
	
	private void checkBeanType(Class<?> beanType, Class<?> objClass) throws BeanCheckException{
		if(!beanType.isAssignableFrom(objClass)){
			throw new BeanCheckException(objClass, "this object is not an instance of " + beanType);
		}
	}
	
	private <T> T doBeanPostProcess(T obj) throws BeanProcessException{
		
		//Bean后处理
		for(DefaultBeanProcessorChain chain: beanPostProcessors){
			if(!chain.isMatch(obj.getClass())){
				continue;
			}
			obj = cloneProcessorChain(chain).doProcess(obj);
		}
		
		//后处理完成通知
		if(obj instanceof IProcessingBean){
			((IProcessingBean) obj).afterProcessComplete();
		}
		
		return obj;
	}
	
	private void doBeanDestroyProcess(Object obj) throws BeanProcessException{
		//Bean后处理
		for(DefaultBeanProcessorChain chain: beanDestroyProcessors){
			if(!chain.isMatch(obj.getClass())){
				continue;
			}
			obj = cloneProcessorChain(chain).doProcess(obj);
		}
	}
	
	private IBeanProcessorChain cloneProcessorChain(IBeanProcessorChain chain) throws BeanProcessException{
		try {
			return (IBeanProcessorChain)chain.clone();
		} catch (CloneNotSupportedException e) {
			throw new BeanProcessException("can not clone ProcessorChain", e);
		}
	}
	
	@Override
	public synchronized void registBeanPostProcessor(Class<?> matchType, IBeanPostProcessor beanProcessor) {
		DefaultBeanProcessorChain chain = getOrCreateProcessorChain(matchType, beanPostProcessors);
		addToProcessorChain(beanProcessor, chain);
	}
	
	@Override
	public synchronized void registBeanDestroyProcessor(Class<?> matchType, IBeanDestroyProcessor beanProcessor) {
		DefaultBeanProcessorChain chain = getOrCreateProcessorChain(matchType, beanDestroyProcessors);
		addToProcessorChain(beanProcessor, chain);
	}
	
	private void addToProcessorChain(IBeanProcessor beanProcessor, DefaultBeanProcessorChain chain){
		if(beanProcessor.priority() > 0){
			chain.addFirstBeanProcessor(beanProcessor);
		}else{
			chain.addLastBeanProcessor(beanProcessor);
		}
	}
	
	private DefaultBeanProcessorChain getOrCreateProcessorChain(Class<?> matchClass, List<DefaultBeanProcessorChain> processChains){
		for(DefaultBeanProcessorChain chain: processChains){
			if(chain.getMatchClass() == matchClass){
				return chain;
			}
		}
		
		DefaultBeanProcessorChain chain = new DefaultBeanProcessorChain(matchClass);
		processChains.add(chain);
		return chain;
	}

	@Override
	public void destroyBean(String beanName) throws BeanNotFoundException, BeanProcessException {
		Object bean = cache.getIfPresent(beanName);
		if(bean != null){
			doBeanDestroyProcess(bean);
			
			cache.invalidate(beanName);
		}else if(callbacks.remove(beanName) == null){
			throw new BeanNotFoundException(beanName);
		}
		
		Iterator<Map.Entry<Class<?>,String> > it = beanTypes.entrySet().iterator();
		while(it.hasNext()){
			String name = it.next().getValue();
			if(name == null || beanName.equals(name)){
				it.remove();
			}
		}
		
		log.debug("Destroyed bean {}", beanName);
	}

	@Override
	public void destroyBean(Class<?> beanClass) throws BeanNotFoundException, BeanProcessException {
		Bean bean = beanClass.getAnnotation(Bean.class);
		if(bean == null){
			throw new BeanNotFoundException(beanClass.getName(), "This class does not annotate @Bean");
		}
		
		String beanName = extractBeanName(bean, beanClass);
		destroyBean(beanName);
	}
	
	@Override
	public void destroyAll() throws BeanProcessException {
		log.info("Begin to destroy all beans");
		
		Set<String> keys = cache.asMap().keySet();
		Set<String> beanNames = new HashSet<String>(keys);
		
		for(String beanName : beanNames) {
			try {
				destroyBean(beanName);
			} catch (BeanNotFoundException e) {
				log.warn("Destroy the bean {} failed, because: {}", beanName, e.getMessage());
			}
		}
		
	}
}
