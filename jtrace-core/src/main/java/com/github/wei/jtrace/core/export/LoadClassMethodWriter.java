package com.github.wei.jtrace.core.export;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadClassMethodWriter extends AdviceAdapter implements Opcodes {
    static Logger logger = LoggerFactory.getLogger("LoadClassMethodWriter");

    private final Type TYPE_INTERCEPTOR = Type.getType("Lcom/github/wei/jtrace/agent/ClassLoaderInterceptor;");

    private final Type TYPE_CLASS = Type.getType(Class.class);
    private final Method METHOD_LOAD_CLASS = Method.getMethod("Class loadClass(String)");
    private final Label LABEL_IF_TRUE = new Label();


    private final String CLASS_NAME;
    private String name, descr;

    protected LoadClassMethodWriter(String className, MethodVisitor mv, int access, String name, String desc) {
        super(ASM5, mv, access, name, desc);
        this.CLASS_NAME = className;
        this.name = name;
        this.descr = desc;
    }

    /**
     * 将NULL推入堆栈
     */
    protected void pushNull() {
        push((Type) null);
    }


    private void invokerInterceptorLoadClass() {
        invokeStatic(TYPE_INTERCEPTOR, METHOD_LOAD_CLASS);
    }


    @Override
    protected void onMethodEnter() {
        loadArg(0);
        invokerInterceptorLoadClass();

        int local = newLocal(TYPE_CLASS);
        storeLocal(local);

        loadLocal(local);
        ifNull(LABEL_IF_TRUE);

        loadLocal(local);
        returnValue();

        visitLabel(LABEL_IF_TRUE);

        if(logger.isDebugEnabled()) {
            logger.debug("Transforming method {}.{}{}", CLASS_NAME, name, descr);
        }
    }

}