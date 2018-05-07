package com.github.wei.jtrace.core.util;

import org.objectweb.asm.Opcodes;

public class ModifierUtil {

	public static String toString(int mod) {
        StringBuffer sb = new StringBuffer();
        int len;

        if ((mod & Opcodes.ACC_PUBLIC) != 0)        sb.append("public ");
        if ((mod & Opcodes.ACC_PROTECTED) != 0)     sb.append("protected ");
        if ((mod & Opcodes.ACC_PRIVATE) != 0)       sb.append("private ");

        /* Canonical order */
        if ((mod & Opcodes.ACC_ABSTRACT) != 0)      sb.append("abstract ");
        if ((mod & Opcodes.ACC_STATIC) != 0)        sb.append("static ");
        if ((mod & Opcodes.ACC_FINAL) != 0)         sb.append("final ");
        if ((mod & Opcodes.ACC_TRANSIENT) != 0)     sb.append("transient ");
        if ((mod & Opcodes.ACC_VOLATILE) != 0)      sb.append("volatile ");
        if ((mod & Opcodes.ACC_SYNCHRONIZED) != 0)  sb.append("synchronized ");
        if ((mod & Opcodes.ACC_NATIVE) != 0)        sb.append("native ");
        if ((mod & Opcodes.ACC_STRICT) != 0)        sb.append("strictfp ");
        if ((mod & Opcodes.ACC_INTERFACE) != 0)     sb.append("interface ");

        if ((len = sb.length()) > 0)    /* trim trailing space */
            return sb.toString().substring(0, len-1);
        return "";
    }
}
