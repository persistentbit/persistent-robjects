package com.persistentbit.robjects;

import com.persistentbit.core.Immutable;
import com.persistentbit.core.collections.PList;

/**
 * @author Peter Muys
 * @since 30/08/2016
 */
@Immutable
public class RCall {
    private final PList<RMethodCall>  callStack;
    private final RMethodCall   thisCall;

    public RCall(PList<RMethodCall> callStack, RMethodCall thisCall) {
        this.callStack = callStack;
        this.thisCall = thisCall;
    }
    public RCall() {
        this(PList.empty(),null);
    }

    public PList<RMethodCall> getCallStack() {
        return callStack;
    }

    public RMethodCall getThisCall() {
        return thisCall;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RCall call = (RCall) o;

        if (!callStack.equals(call.callStack)) return false;
        return thisCall.equals(call.thisCall);

    }

    @Override
    public int hashCode() {
        int result = callStack.hashCode();
        result = 31 * result + thisCall.hashCode();
        return result;
    }
}
