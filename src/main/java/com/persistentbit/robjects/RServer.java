package com.persistentbit.robjects;



import com.persistentbit.robjects.annotations.Remotable;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.function.Supplier;


public class RServer<R> {
    private final Class<R>   rootInterface;
    private final Supplier<R> rootSupplier;

    public RServer(Class<R> rootInterface, Supplier<R> rootSupplier) {
        this.rootInterface = Objects.requireNonNull(rootInterface);
        this.rootSupplier = Objects.requireNonNull(rootSupplier);
    }


    public R createRootProxy(){
        return (R)Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class<?>[]{rootInterface},new RProxy(this,null,rootInterface));
    }

    public RCallResult  rootCall(RCall call){
        return call(rootInterface,rootSupplier,call);
    }

    public RCallResult call(Class remoteInterface, Object obj, RCall call){

        MethodDefinition md = call.getMethodToCall();
        try{
            if(obj == null){
                throw new RuntimeException("Can't call on null: " + md);
            }
            Method m = remoteInterface.getMethod(md.getMethodName(),md.getParameters().map(pd ->{ try {
                return Class.forName(pd.getTypeName());
            }catch (Exception e){
                throw new RuntimeException(e);
            }}).toArray());
            Object result = m.invoke(obj,call.getArguments());
            RCall nextCall = call.getNextCall().orElse(null);
            if(nextCall == null){
                return new RCallResult(call,result,null);
            }
            Class resultType = m.getReturnType();
            return call(resultType,result,nextCall);

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public Class<?>  getRemotableClass(Class<?> cls){

        if(cls.getDeclaredAnnotation(Remotable.class) != null){
            return cls;
        }
        for(Class<?> i : cls.getInterfaces()){
            Class<?> resCls = getRemotableClass(i);
            if(resCls != null){
                return resCls;
            }
        }

        return null;
    }
}
