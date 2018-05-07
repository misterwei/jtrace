package com.github.wei.jtrace.core.beans;

import java.lang.reflect.Field;

import com.github.wei.jtrace.api.beans.BeanRef;
import com.github.wei.jtrace.api.beans.IBeanFactory;
import com.github.wei.jtrace.api.beans.IBeanPostProcessor;
import com.github.wei.jtrace.api.beans.IBeanProcessorChain;
import com.github.wei.jtrace.api.exception.BeanNotFoundException;
import com.github.wei.jtrace.api.exception.BeanProcessException;

public class BeanRefInjectProcessor implements IBeanPostProcessor {
	
	private IBeanFactory beanFactory;
	public BeanRefInjectProcessor(IBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	
	@Override
	public int priority() {
		return 1;
	}

	@Override
	public <T> T process(T obj, IBeanProcessorChain chain) throws BeanProcessException {
		try{
			Class<?> beanClass = obj.getClass();
			Field[] fields = beanClass.getDeclaredFields();
			for(Field field: fields){
				BeanRef ref = field.getAnnotation(BeanRef.class);
				if(ref != null){
					String beanName = ref.name();
					if(!beanFactory.containsBean(beanName)){
						throw new BeanNotFoundException(beanName);
					}
					
					boolean acc = field.isAccessible();
					if(!acc){
						field.setAccessible(true);
					}
					
					Object bean = beanFactory.getBean(beanName);
					field.set(obj, bean);
					
					if(!acc){
						field.setAccessible(acc);
					}
				}
			}
			
			return chain.doProcess(obj);
		}catch(Exception e){
			throw new BeanProcessException(e.getMessage(), e);
		}
	}

}
