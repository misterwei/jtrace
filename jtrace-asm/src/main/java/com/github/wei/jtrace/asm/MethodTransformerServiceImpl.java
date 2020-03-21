package com.github.wei.jtrace.asm;

import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.transform.ITransformService;
import com.github.wei.jtrace.asm.api.IMethodTransformerMatcher;
import com.github.wei.jtrace.asm.api.IMethodTransformerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

@Bean(type = IMethodTransformerService.class)
public class MethodTransformerServiceImpl implements IMethodTransformerService {
    private Logger logger = LoggerFactory.getLogger(MethodTransformerServiceImpl.class);

    private ConcurrentHashMap<IMethodTransformerMatcher, Integer> map = new ConcurrentHashMap<IMethodTransformerMatcher, Integer>();

    @AutoRef
    ITransformService transformService;

    @Override
    public boolean addMethodTransformerMatcher(IMethodTransformerMatcher transformer, boolean refresh) {
        try {
            int id = transformService.registerTransformerMatcher(new TransformerMatcherWrapper(transformer), refresh);
            map.put(transformer, id);
            return true;
        } catch (IllegalAccessException e) {
            logger.error("Failed to add method transformer matcher", e);
        }
        return false;
    }

    @Override
    public void refreshMethodTransformerMatcher(IMethodTransformerMatcher transformer) {
        Integer id = map.remove(transformer);
        if(id != null){
            transformService.refreshTransformerById(id);
        }
    }

    @Override
    public void removeMethodTransformerMatcher(IMethodTransformerMatcher transformer) {
        Integer id = map.remove(transformer);
        if(id != null){
            transformService.removeTransformerMatcherById(id);
        }
    }
}
