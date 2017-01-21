package com.persistentbit.substema;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.jjson.mapping.impl.JJObjectReader;
import com.persistentbit.jjson.nodes.JJNode;
import com.persistentbit.jjson.nodes.JJNodeArray;
import com.persistentbit.jjson.nodes.JJNodeObject;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;


public class RemoteObjectDefinition implements Serializable
{

    private final Class<?>                       remoteObjectClass;
    private final PList<MethodDefinition>        remoteMethods;
    private final PMap<MethodDefinition, Result> remoteCached;
    private final RCallStack                     callStack;


    public RemoteObjectDefinition(Class<?> remoteObjectClass, PList<MethodDefinition> remoteMethods,
                                  PMap<MethodDefinition, Result> remoteCached, RCallStack callStack
    ) {
        this.remoteObjectClass = Objects.requireNonNull(remoteObjectClass);
        this.callStack = Objects.requireNonNull(callStack);
        this.remoteMethods = Objects.requireNonNull(remoteMethods);
        this.remoteCached = Objects.requireNonNull(remoteCached);
    }

    public PList<MethodDefinition> getRemoteMethods() {
        return remoteMethods;
    }

    public PMap<MethodDefinition, Result> getRemoteCached() {
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

    public static final JJObjectReader jsonReader = (type, node, masterReader) ->
        Log.function().code(l -> {
            JJNodeObject obj       = node.asObject().orElseThrow();
            Class        cls       = masterReader.read(obj.get("remoteObjectClass").orElse(null), Class.class);
            RCallStack   callStack = masterReader.read(obj.get("callStack").get(), RCallStack.class);
            JJNodeArray  mdArr     = obj.get("remoteMethods").get().asArray().orElseThrow();
            PList<MethodDefinition> remoteMethods = mdArr.pstream().map(item ->
                                                                            masterReader
                                                                                .read(item, MethodDefinition.class)
            ).plist();
            JJNodeArray cachedArr = obj.get("remoteCached").get().asArray().orElseThrow();
            PMap<MethodDefinition, Result> cached = PStream.toMap(cachedArr.pstream().map(itemObj -> {
                PList<JJNode>    keyValueNodes = itemObj.asArray().orElseThrow().pstream().plist();
                MethodDefinition itemMd        = masterReader.read(keyValueNodes.get(0), MethodDefinition.class);
                Method           m             = RemotableMethods.getRemotableMethod(itemMd);
                Type             typeValue     = m.getGenericReturnType();
                Result           result        = masterReader.read(keyValueNodes.get(1), Result.class, typeValue);
                return Tuple2.of(itemMd, result);
            }));
            return new RemoteObjectDefinition(cls, remoteMethods, cached, callStack);
        });

}
