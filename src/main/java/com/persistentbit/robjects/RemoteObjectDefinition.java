package com.persistentbit.robjects;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;

import java.io.Serializable;
import java.util.Objects;


public class RemoteObjectDefinition implements Serializable
{
    private final String typeName;
    private final PSet<MethodDefinition> remoteMethods;
    private final PMap<MethodDefinition,Object> remoteCached;

    private RemoteObjectDefinition() {
        this.typeName = null;
        this.remoteMethods = null;
        this.remoteCached = null;
    }

    public RemoteObjectDefinition(
            Class<?> typeClass,
            PSet<MethodDefinition> remoteMethods,
            PMap<MethodDefinition, Object> remoteCached)
    {
        this.typeName = Objects.requireNonNull(typeClass).getName();
        this.remoteMethods = Objects.requireNonNull(remoteMethods);
        this.remoteCached = Objects.requireNonNull(remoteCached);
    }

    public String getTypeName()
    {
        return typeName;
    }

    public PSet<MethodDefinition> getRemoteMethods()
    {
        return remoteMethods;
    }

    public PMap<MethodDefinition, Object> getRemoteCached()
    {
        return remoteCached;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RemoteObjectDefinition that = (RemoteObjectDefinition) o;

        if (!typeName.equals(that.typeName))
            return false;
        if (!remoteMethods.equals(that.remoteMethods))
            return false;
        return true;

    }

    @Override
    public int hashCode()
    {
        int result = typeName.hashCode();
        result = 31 * result + remoteMethods.hashCode();
        result = 31 * result + remoteCached.hashCode();
        return result;
    }
}
