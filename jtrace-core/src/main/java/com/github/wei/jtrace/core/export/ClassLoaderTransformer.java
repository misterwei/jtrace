package com.github.wei.jtrace.core.export;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.TransformException;
import com.github.wei.jtrace.api.transform.ITransformer;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

public class ClassLoaderTransformer implements ITransformer {
    private final String JAVA_OBJECT_TYPE = "java/lang/Object";
    @Override
    public byte[] transform(final ClassLoader loader, IClassDescriberTree descr, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws TransformException {

        ClassReader cr = new ClassReader(classfileBuffer);

        if((cr.getAccess() & ACC_INTERFACE) != 0) {
            return null;
        }

        String className = descr.getClassDescriber().getName();

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
                    return "java/lang/Object";
                } else {
                    do {
                        c = c.getSuperclass();
                    } while (!c.isAssignableFrom(d));
                    return c.getName().replace('.', '/');
                }
            }
        };
        cr.accept(new ClassLoaderInterceptorWriter(className, cw), ClassReader.EXPAND_FRAMES);

        return cw.toByteArray();
    }

    private class ClassLoaderInterceptorWriter extends ClassVisitor implements Opcodes {
        final Logger logger = LoggerFactory.getLogger("ClassLoaderInterceptorWriter");
        private final String METHOD_NAME = "loadClass";
        private final String METHOD_DESC = "(Ljava/lang/String;)Ljava/lang/Class;";
        private final String CLASS_NAME;
        public ClassLoaderInterceptorWriter(String className, ClassVisitor cv) {
            super(ASM5, cv);
            this.CLASS_NAME = className;
        }

        @Override
        public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

            if(METHOD_NAME.equals(name) && METHOD_DESC.equals(desc)){
                logger.info("Transforming method {}.{}{} ", CLASS_NAME, name, desc);

                return new LoadClassMethodWriter(CLASS_NAME, mv, access, name, desc);
            }

            return mv;
        }
    }

}
