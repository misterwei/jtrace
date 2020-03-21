package com.github.wei.jtrace.asm;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.ClassMatchException;
import com.github.wei.jtrace.api.transform.ITransformer;
import com.github.wei.jtrace.api.transform.ITransformerMatcher;
import com.github.wei.jtrace.asm.api.IMethodTransformer;
import com.github.wei.jtrace.asm.api.IMethodTransformerMatcher;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

public class TransformerMatcherWrapper implements ITransformerMatcher, ITransformer {
    private final String JAVA_OBJECT_TYPE = "java/lang/Object";

    private IMethodTransformerMatcher methodTransformerMatcher;

    public TransformerMatcherWrapper(IMethodTransformerMatcher methodTransformerMatcher){
        this.methodTransformerMatcher = methodTransformerMatcher;
    }

    @Override
    public ITransformer matchedTransformer(IClassDescriberTree classTree) throws ClassMatchException {
        if(!methodTransformerMatcher.matchClass(classTree)){
            return null;
        }
        return this;
    }

    @Override
    public boolean matchClass(IClassDescriberTree descr) throws ClassMatchException {
        return methodTransformerMatcher.matchClass(descr);
    }

    @Override
    public byte[] transform(final ClassLoader loader, IClassDescriberTree descr,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode, ClassReader.EXPAND_FRAMES);

        boolean modified = false;
        List<MethodNode> methodNodes =  classNode.methods;
        for(int i=0;i<methodNodes.size();i++){
            MethodNode mn = methodNodes.get(i);
            IMethodTransformer imt = methodTransformerMatcher.matchedTransformer(loader, descr.getClassDescriber(), mn);
            if(imt != null){
                modified = true;
                mn = imt.transform(mn);
                if(mn != null) {
                    methodNodes.set(i, mn);
                }
            }
        }

        if(!modified){
            return null;
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {

            /**
             * 因为ClassLoader隔离，所以在ASM所在的ClassLoader中找不到应用的类，
             * 真正的ClassLoader应该是transform传递进来的ClassLoader
             */
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                if(JAVA_OBJECT_TYPE.equals(type1) || JAVA_OBJECT_TYPE.equals(type2)){
                    return JAVA_OBJECT_TYPE;
                }

                Class<?> c, d;
                try {

                    c = Class.forName(type1.replace('/', '.'), false, loader);
                    d = Class.forName(type2.replace('/', '.'), false, loader);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (c.isAssignableFrom(d)) {
                    return type1;
                }
                if (d.isAssignableFrom(c)) {
                    return type2;
                }
                if (c.isInterface() || d.isInterface()) {
                    return JAVA_OBJECT_TYPE;
                } else {
                    do {
                        c = c.getSuperclass();
                    } while (!c.isAssignableFrom(d));
                    return c.getName().replace('.', '/');
                }
            }
        };
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
