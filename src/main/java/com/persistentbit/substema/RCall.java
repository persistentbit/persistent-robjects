package com.persistentbit.substema;

import com.persistentbit.core.Immutable;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 30/08/2016
 */
@Immutable
public class RCall extends BaseValueClass{
    private final RSessionData  sessionData;
    private final RCallStack  callStack;
    private final RMethodCall   thisCall;

    public RCall(RSessionData sessionData,RCallStack callStack, RMethodCall thisCall) {
        this.sessionData = sessionData;
        this.callStack = callStack;
        this.thisCall = thisCall;
    }
    public RCall(RSessionData sessionData,RCallStack callStack) {
        this(sessionData,callStack,null);
    }

    public RCallStack getCallStack() {
        return callStack;
    }

    public RMethodCall getThisCall() {
        return thisCall;
    }

    public RSessionData getSessionData() {
        return sessionData;
    }
}
