package com.github.wei.jtrace.api.transform;

import com.github.wei.jtrace.api.clazz.IClassDescriberTree;
import com.github.wei.jtrace.api.exception.TransformException;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public interface ITransformer {

    byte[]
    transform(  ClassLoader         loader,
                IClassDescriberTree descr,
                Class<?>            classBeingRedefined,
                ProtectionDomain protectionDomain,
                byte[]              classfileBuffer)
            throws TransformException;
}
