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
    public byte[] transform(ClassLoader loader, IClassDescriberTree descr,
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
            IMethodTransformer imt = methodTransformerMatcher.matchedTransformer(mn);
            if(imt != null){
                modified = true;
                mn = imt.transform(loader, descr.getClassDescriber(), mn);
                if(mn != null) {
                    methodNodes.set(i, mn);
                }
            }
        }

        if(!modified){
            return null;
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
