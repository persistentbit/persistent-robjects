package com.persistentbit.robjects;

import com.persistentbit.core.Nullable;

import java.util.Optional;

public class RCall {
    private final MethodDefinition  methodToCall;
    private final Object[]   arguments;
    @Nullable  private final RCall nextCall;

    public RCall(MethodDefinition md,Object[] arguments){
        this(md,arguments,null);
    }

    public RCall(MethodDefinition methodToCall,Object[] arguments,RCall nextCall){
        this.methodToCall = methodToCall;
        this.arguments = arguments;
        this.nextCall = nextCall;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public MethodDefinition getMethodToCall() {
        return methodToCall;
    }

    public Optional<RCall> getNextCall() {
        return Optional.ofNullable(nextCall);
    }
}
