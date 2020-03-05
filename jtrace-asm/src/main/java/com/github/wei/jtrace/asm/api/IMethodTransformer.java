package com.github.wei.jtrace.asm.api;

import org.objectweb.asm.tree.MethodNode;

public interface IMethodTransformer {

    MethodNode transform(MethodNode methodNode);
}
