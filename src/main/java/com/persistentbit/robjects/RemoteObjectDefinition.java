package com.persistentbit.robjects;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.jjson.utils.ObjectWithTypeName;

import java.io.Serializable;
import java.util.Objects;


public class RemoteObjectDefinition implements Serializable
{
    private final Class<?>  remoteObjectClass;
    private final PList<MethodDefinition> remoteMethods;
    private final PMap<MethodDefinition,ObjectWithTypeName> remoteCached;
    private final RCallStack callStack;





    public RemoteObjectDefinition(Class<?> remoteObjectClass, PList<MethodDefinition> remoteMethods, PMap<MethodDefinition, ObjectWithTypeName> remoteCached, RCallStack callStack) {
        this.remoteObjectClass = Objects.requireNonNull(remoteObjectClass);
        this.callStack = Objects.requireNonNull(callStack);
        this.remoteMethods = Objects.requireNonNull(remoteMethods);
        this.remoteCached = Objects.requireNonNull(remoteCached);
    }

    public PList<MethodDefinition> getRemoteMethods() {
        return remoteMethods;
    }

    public PMap<MethodDefinition, ObjectWithTypeName> getRemoteCached() {
        return remoteCached;
    }

    public RCallStack getCallStack() {
        return callStack;
    }


    public Class<?> getRemoteObjectClass() {
        return remoteObjectClass;
    }

    @Override
    public String toString() {
        return "RemoteObjectDefinition{" +
                "remoteObjectClass=" + remoteObjectClass +
                ", remoteMethods=" + remoteMethods +
                ", remoteCached=" + remoteCached +
                ", callStack=" + callStack +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteObjectDefinition that = (RemoteObjectDefinition) o;

        if (!remoteObjectClass.equals(that.remoteObjectClass)) return false;
        if (!remoteMethods.equals(that.remoteMethods)) return false;
        if (!remoteCached.equals(that.remoteCached)) return false;
        return callStack.equals(that.callStack);

    }

    @Override
    public int hashCode() {
        int result = remoteObjectClass.hashCode();
        result = 31 * result + remoteMethods.hashCode();
        result = 31 * result + remoteCached.hashCode();
        result = 31 * result + callStack.hashCode();
        return result;
    }
}
