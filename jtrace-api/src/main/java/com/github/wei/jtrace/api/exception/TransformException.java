package com.github.wei.jtrace.api.exception;

public class TransformException extends Exception {
    /**
     * Create a new BeanInstantiationException.
     * @param msg the offending bean class
     * @param msg the detail message
     */
    public TransformException(String msg) {
        super(msg);
    }

    public TransformException(Throwable t) {
        super(t);
    }

    /**
     * Create a new BeanInstantiationException.
     * @param msg the detail message
     * @param cause the root cause
     */
    public TransformException(String msg, Throwable cause) {
        super( msg, cause);
    }
}
