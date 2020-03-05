package com.github.wei.jtrace.api.exception;

public class ServiceStopException extends Exception {
    public ServiceStopException(String message){
        super(message);
    }

    public ServiceStopException(Throwable t){
        super(t);
    }

    public ServiceStopException(String message, Throwable t){
        super(message, t);
    }
}
