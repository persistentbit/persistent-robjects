package com.persistentbit.robjects;

import java.util.Arrays;

public class RMethodCall {
    private final MethodDefinition  methodToCall;
    private final Object[]          arguments;



    public RMethodCall(MethodDefinition methodToCall, Object[] arguments){
        this.methodToCall = methodToCall;
        this.arguments = arguments;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public MethodDefinition getMethodToCall() {
        return methodToCall;
    }





    @Override
    public String toString() {
        return "RMethodCall{" +
                "methodToCall=" + methodToCall +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
    }
}
