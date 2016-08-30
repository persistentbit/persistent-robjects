package com.persistentbit.robjects;

import com.persistentbit.core.Immutable;

import java.util.Optional;

@Immutable
public class RCallResult {
    private final Object                    value;
    private final RemoteObjectDefinition    robject;
    private final Exception exception;



    public RCallResult(Object value, RemoteObjectDefinition robject, Exception exception) {
        this.value = value;
        this.robject = robject;
        this.exception = exception;
    }

    public Optional<Object> getValue() {
        return Optional.ofNullable(value);
    }

    public Optional<RemoteObjectDefinition> getRobject() {
        return Optional.ofNullable(robject);
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    static public RCallResult value(Object v){
        return new RCallResult(v,null,null);
    }
    static public RCallResult robject(RemoteObjectDefinition r){
        return new RCallResult(null,r,null);
    }
    static public RCallResult exception(Exception r){
        return new RCallResult(null,null,r);
    }

}
