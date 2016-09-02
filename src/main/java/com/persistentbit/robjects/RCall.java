package com.persistentbit.robjects;

import com.persistentbit.core.Immutable;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 30/08/2016
 */
@Immutable
public class RCall extends BaseValueClass{
    private final RCallStack  callStack;
    private final RMethodCall   thisCall;

    public RCall(RCallStack callStack, RMethodCall thisCall) {
        this.callStack = callStack;
        this.thisCall = thisCall;
    }
    public RCall(RCallStack callStack) {
        this(callStack,null);
    }

    public RCallStack getCallStack() {
        return callStack;
    }

    public RMethodCall getThisCall() {
        return thisCall;
    }



}
