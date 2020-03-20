package com.github.wei.jtrace.asm.api;

import com.github.wei.jtrace.api.clazz.ClassDescriber;
import org.objectweb.asm.tree.MethodNode;

public interface IMethodTransformer {

    MethodNode transform(ClassLoader loader, ClassDescriber classDescriber, MethodNode methodNode);
}
