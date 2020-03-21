package com.github.wei.jtrace.asm.api;

public interface IMethodTransformerService {

    boolean addMethodTransformerMatcher(IMethodTransformerMatcher transformer, boolean refresh);

    void refreshMethodTransformerMatcher(IMethodTransformerMatcher transformer);

    void removeMethodTransformerMatcher(IMethodTransformerMatcher transformer);
}
