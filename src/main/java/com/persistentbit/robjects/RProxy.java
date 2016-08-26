package com.persistentbit.robjects;



import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


public class RProxy implements InvocationHandler {

    public RServer server;
    public RCallResult callResult;
    public Class    remotableClass;

    public RProxy(RServer server, RCallResult callResult, Class remotableClass) {
        this.server = server;
        this.callResult = callResult;
        this.remotableClass = remotableClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RCallResult thisResult  = null;
        RCall call = new RCall(new MethodDefinition(method),args);
        if(callResult == null){


            thisResult = server.rootCall(call);
        } else {
            thisResult = server.call(remotableClass,this,call);
        }
        throw new RuntimeException("NotYet");
    }
}
