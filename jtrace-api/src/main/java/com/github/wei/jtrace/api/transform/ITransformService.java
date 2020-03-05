package com.github.wei.jtrace.api.transform;

import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;

public interface ITransformService {

    int registerTransformerMatcher(ITransformerMatcher transformerMatcher, boolean refresh) throws IllegalAccessException;

    int registerTransformerMatcherImmediately(ITransformerMatcher transformerMatcher) throws IllegalAccessException;

    ITransformerMatcher getTransformerMatcherById(int id);

    void removeTransformerMatcherById(int id);

    void removeTransformerMatcher(ITransformerMatcher transformer);

    boolean refreshTransformerById(int id);

    boolean refreshTransformer(IClassMatcher transformer);

}
