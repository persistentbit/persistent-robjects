package com.persistentbit.robjects;


public class RCallResult {
    private final RCall call;
    private final Object valueResult;

    private final Exception   exception;

    public RCallResult(RCall call, Object result, Exception exception) {
        this.call = call;
        this.valueResult = result;
        this.exception = exception;
    }

    public RCall getCall() {
        return call;
    }

    public Object getResult() {
        return valueResult;
    }

    public Exception getException() {
        return exception;
    }
}
