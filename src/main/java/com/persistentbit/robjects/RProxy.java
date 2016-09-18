package com.persistentbit.robjects;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * An RProxy is a Interface Proxy for Remote Objects that uses a {@link RemoteService} to
 * execute the method calls.
 */
public class RProxy implements InvocationHandler {


    private final RemoteService server;
    private final RemoteObjectDefinition rod;

    static private class ClientSessionData{
        private RSessionData sessionData;
        public RSessionData    getSessionData(){
            return sessionData;
        }

        public void setSessionData(RSessionData sessionData){
            this.sessionData = sessionData;
        }

    }

    private final ClientSessionData clientSessionData;


    private RProxy(RemoteService server,ClientSessionData clientSessionData,RemoteObjectDefinition rod) {
        this.server = server;
        this.clientSessionData = clientSessionData;
        this.rod = rod;

    }

    /**
     * Create a new Proxy for the given {@link RemoteObjectDefinition}.<br>
     * The new proxy will share the {@link ClientSessionData} with this proxy.<br>
     *
     * @param server    The RemoteService
     * @param clientSessionData The SessionData (originated from the root service proxy)
     * @param rod The Remote Object Definintion for this proxy
     * @param <C> The Result interface type
     * @return A new Interface proxy
     */
    static private <C> C create(RemoteService server,ClientSessionData clientSessionData, RemoteObjectDefinition rod){
        return (C)Proxy.newProxyInstance(RProxy.class.getClassLoader(),new Class<?>[]{rod.getRemoteObjectClass()},new RProxy(server,clientSessionData,rod));
    }



    /**
     * Create a new Proxy for the root Remote Object, using the given {@link RemoteService} to execute the calls.
     * @param server The RemoteService
     * @param <C> The type of the Root Remote Object
     * @return The Proxy
     */
    static public <C> C create(RemoteService server){
        try{
            return create(server,new ClientSessionData(),server.getRoot().get().getRobject().get());
        }catch (Exception e){
            throw new RObjException(e);
        }
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        MethodDefinition md = new MethodDefinition(rod.getRemoteObjectClass(),method);
        if(rod.getRemoteCached().containsKey(md)){
            Object cached = rod.getRemoteCached().get(md).getValue();
            return cached;
        }
        //Create The Call
        RCall call = new RCall(clientSessionData.getSessionData(),rod.getCallStack(),new RMethodCall(md,args));

        //Execute the Call
        return server.call(call)
                .thenApply(result -> {

                    //Save the new Session Data
                    clientSessionData.setSessionData(result.getSessionData().orElse(null));

                    //If the result is an exception: Throw it.
                    result.getException().ifPresent(e ->{ throw new RuntimeException("Remote exception",e);});

                    //If the result is a value: Return it;
                    if(result.getValue().isPresent()){
                        return result.getValue().get();
                    }

                    //Must be remote object
                    RemoteObjectDefinition rod = result.getRobject().orElse(null);
                    if (rod == null) {
                        return null;
                    }

                    return RProxy.create(server,clientSessionData,rod);
                });

    }
}
