package com.github.wei.jtrace.core.export;

import com.github.wei.jtrace.api.IExportClassService;
import com.github.wei.jtrace.api.beans.AutoRef;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.api.beans.IProcessingBean;
import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.BeanProcessException;
import com.github.wei.jtrace.api.exception.ClassMatchException;
import com.github.wei.jtrace.api.transform.ITransformService;
import com.github.wei.jtrace.api.transform.ITransformer;
import com.github.wei.jtrace.api.transform.ITransformerMatcher;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import com.github.wei.jtrace.api.transform.matcher.BaseClassMatcher;
import com.github.wei.jtrace.core.JtraceLauncher;

@Bean(type=IExportClassService.class)
public class ExportClassService implements IExportClassService, IProcessingBean, ITransformerMatcher {

    private IClassMatcher classMatcher =
            new BaseClassMatcher("java/lang/ClassLoader", "java/net/URLClassLoader");

    private ClassLoaderTransformer transformer = new ClassLoaderTransformer();

    @AutoRef
    ITransformService transformService;

    public boolean exportClass(ClassLoader classLoader, String className){
        ClassLoader cl = JtraceLauncher.EXPORT_CLASS.putIfAbsent(className, classLoader);
        if(cl != null){
            return false;
        }
        return true;
    }

    @Override
    public ITransformer matchedTransformer(IClassDescriberTree classTree) throws ClassMatchException {
        if(classMatcher.matchClass(classTree)){
            return transformer;
        }
        return null;
    }

    @Override
    public boolean matchClass(IClassDescriberTree descr) throws ClassMatchException {
        return classMatcher.matchClass(descr);
    }

    @Override
    public void afterProcessComplete() throws BeanProcessException {
        try {
            transformService.registerTransformerMatcherImmediately(this);
        } catch (IllegalAccessException e) {
            throw new BeanProcessException(e);
        }
    }

}
