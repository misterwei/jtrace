package com.github.wei.jtrace.asm.api;

import com.github.wei.jtrace.api.clazz.ClassDescriber;
import com.github.wei.jtrace.api.transform.matcher.IClassMatcher;
import org.objectweb.asm.tree.MethodNode;

public interface IMethodTransformerMatcher extends IClassMatcher {

    IMethodTransformer matchedTransformer(ClassLoader loader, ClassDescriber classDescriber, MethodNode methodNode);
}
