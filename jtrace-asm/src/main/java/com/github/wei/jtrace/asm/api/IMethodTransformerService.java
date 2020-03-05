package com.github.wei.jtrace.asm.api;

public interface IMethodTransformerService {

    boolean addMethodTransformerMatcher(IMethodTransformerMatcher transformer, boolean refresh);

    void removeMethodTransformerMatcher(IMethodTransformerMatcher transformer);
}
