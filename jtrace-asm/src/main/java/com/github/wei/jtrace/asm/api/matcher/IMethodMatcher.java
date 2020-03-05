package com.github.wei.jtrace.asm.api.matcher;

import org.objectweb.asm.tree.MethodNode;

public interface IMethodMatcher {
    boolean match(MethodNode methodNode);
}
