package com.persistentbit.robjects;


import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.utils.ObjectWithTypeName;
import com.persistentbit.robjects.annotations.RemoteCache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;


public class RServer<R> implements RemoteService{
    static private final Logger log = Logger.getLogger(RServer.class.getName());
    private final Class<R>   rootInterface;
    private final Supplier<R> rootSupplier;
    private final JJMapper  mapper;
    private final String    secret;
    public RServer(String secret,Class<R> rootInterface, Supplier<R> rootSupplier){
        this(secret, rootInterface,rootSupplier,new JJMapper());
    }
    public RServer(String secret, Class<R> rootInterface, Supplier<R> rootSupplier,JJMapper mapper) {
        this.secret = secret;
        this.rootInterface = Objects.requireNonNull(rootInterface);
        this.rootSupplier = Objects.requireNonNull(rootSupplier);
        this.mapper = mapper;
    }






    public RCallResult  call(RCall call){

        if(call.getThisCall() == null){
            RemoteObjectDefinition rod =  createROD(RCallStack.createAndSign(PList.empty(),mapper,secret),this.rootInterface,rootSupplier.get());
            return RCallResult.robject(rod);
        }


        try {
            Object result = call(rootSupplier.get(),call.getCallStack());
            result = call(result, call.getThisCall());

            Class<?> remoteClass = result == null ? null : RemotableClasses.getRemotableClass(result.getClass());
            if(remoteClass == null ){
                return RCallResult.value(call.getThisCall().getMethodToCall(),result);
            } else {
                RCallStack newCallStack = RCallStack.createAndSign(call.getCallStack().getCallStack().plus(call.getThisCall()),mapper,secret);
                return RCallResult.robject(createROD(newCallStack,remoteClass,result));
            }
        }catch (Exception e){
            log.severe(e.getMessage());
            return RCallResult.exception(e);
        }

    }

    private RemoteObjectDefinition  createROD(RCallStack call, Class<?> remotableClass, Object obj){
        try {
            PList<MethodDefinition> remoteMethods = PList.empty();
            PMap<MethodDefinition, ObjectWithTypeName> cachedMethods = PMap.empty();
            for (Method m : remotableClass.getDeclaredMethods()) {
                MethodDefinition md = new MethodDefinition(remotableClass,m);
                if (m.getParameterCount() == 0 && m.getDeclaredAnnotation(RemoteCache.class) != null) {
                    Object value = m.invoke(obj);
                    cachedMethods = cachedMethods.put(md, new ObjectWithTypeName(value));
                } else {
                    remoteMethods = remoteMethods.plus(md);
                }
            }
            return new RemoteObjectDefinition(remotableClass,remoteMethods, cachedMethods, call);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

    }


    private Object call(Object obj, RMethodCall call) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{

        MethodDefinition md = call.getMethodToCall();
        //if(obj instanceof Optional){
        //    obj = ((Optional)obj).orElse(null);
        //}
        if(obj == null){
            throw new RuntimeException("Can't call on null: " + md);
        }
        Method m = obj.getClass().getMethod(md.getMethodName(),md.getParamTypes());
        obj = m.invoke(obj,call.getArguments());

        return obj;

    }

    private Object call(Object obj, RCallStack callStack) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{
        if(callStack.verifySignature(secret,mapper) == false){
            throw new RObjException("Wrong signature !!!");
        }
        for(RMethodCall c : callStack.getCallStack()){
            obj = call(obj,c);
        }
        return obj;
    }


}
