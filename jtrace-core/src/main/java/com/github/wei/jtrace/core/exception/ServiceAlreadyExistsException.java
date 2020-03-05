package com.github.wei.jtrace.core.exception;

public class ServiceAlreadyExistsException extends Exception {
    public ServiceAlreadyExistsException(String serviceId){
        super("service(" + serviceId+") already exists");
    }
}
