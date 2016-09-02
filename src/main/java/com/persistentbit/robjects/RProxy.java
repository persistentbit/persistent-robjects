package com.persistentbit.robjects;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


public class RProxy implements InvocationHandler {


    private final RemoteService server;
    private final RemoteObjectDefinition rod;


    private RProxy(RemoteService server,RemoteObjectDefinition rod) {
        this.server = server;
        this.rod = rod;

    }
    static public <C> C create(RemoteService server, RemoteObjectDefinition rod){
        return (C)Proxy.newProxyInstance(RProxy.class.getClassLoader(),new Class<?>[]{rod.getRemoteObjectClass()},new RProxy(server,rod));
    }
    static public <C> C create(RemoteService server){
        return create(server,server.getRoot().getRobject().get());
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodDefinition md = new MethodDefinition(rod.getRemoteObjectClass(),method);
        if(rod.getRemoteCached().containsKey(md)){
            Object cached = rod.getRemoteCached().get(md).getValue();
            return cached;
        }
        RCall call = new RCall(rod.getCallStack(),new RMethodCall(md,args));
        RCallResult result = server.call(call);
        result.getException().ifPresent(e ->{ throw new RuntimeException("Remote exception",e);});
        if(result.getValue().isPresent()){
            return result.getValue().get();
        }
        //Must be remote object
        RemoteObjectDefinition rod = result.getRobject().orElse(null);
        if (rod == null) {
            return null;
        }

        return RProxy.create(server,rod);
    }
}
