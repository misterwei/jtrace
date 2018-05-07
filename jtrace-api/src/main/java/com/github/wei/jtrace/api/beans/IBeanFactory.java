package com.github.wei.jtrace.api.beans;

import java.util.concurrent.ExecutionException;

import com.github.wei.jtrace.api.exception.BeanCheckException;
import com.github.wei.jtrace.api.exception.BeanInstantiationException;
import com.github.wei.jtrace.api.exception.BeanNotFoundException;
import com.github.wei.jtrace.api.exception.BeanProcessException;

public interface IBeanFactory {
	
	<T> T getBean(String beanName) throws ExecutionException;
	
	<T> T getBean(Class<T> beanClass) throws ExecutionException;
	
	boolean containsBean(String beanName);
	
	<T> T registBean(Class<T> beanClass) throws BeanInstantiationException, BeanCheckException, BeanProcessException;
	
	<T> T registObject(String beanName, T bean) throws BeanCheckException, BeanProcessException;
	
	void registLazyBean(Class<?> beanClass) throws BeanCheckException;
	
	void destroyBean(String beanName) throws BeanNotFoundException, BeanProcessException;
	
	void destroyBean(Class<?> beanClass) throws BeanNotFoundException, BeanProcessException;
	
	void destroyAll() throws BeanProcessException;
	
	void registBeanPostProcessor(Class<?> clazz,IBeanPostProcessor beanProcessor);
	
	void registBeanDestroyProcessor(Class<?> clazz,IBeanDestroyProcessor beanProcessor);
}
