package com.github.wei.jtrace.api.exception;

public class ServiceStartException extends Exception {
    public ServiceStartException(String message){
        super(message);
    }

    public ServiceStartException(Throwable t){
        super(t);
    }

    public ServiceStartException(String message, Throwable t){
        super(message, t);
    }
}
