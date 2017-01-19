package com.persistentbit.substema;


import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.ReflectionUtils;
import com.persistentbit.substema.annotations.Remotable;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * User: petermuys
 * Date: 30/10/15
 * Time: 17:23
 */
public final class RemotableClasses{

    private static Map<Class<?>, Optional<Class<?>>> remoteClasses = new HashMap<>();


    public static boolean returnsRemotable(Method m) {
        if(m.getReturnType().equals(Result.class) == false) {
            throw new RuntimeException("Expected a result type");
        }
        ParameterizedType pt = (ParameterizedType) m.getGenericReturnType();
        return getRemotableClass(ReflectionUtils.classFromType(pt.getActualTypeArguments()[0])) != null;
    }

    public static Class<?> getRemotableClass(Class<?> cls) {
        if(cls == CompletableFuture.class){
            throw new RuntimeException("Unreference ComputableFuture");
        }

        Optional<Class<?>> res = remoteClasses.get(cls);
        if(res != null){
            return res.orElse(null);
        }
        if(cls.getDeclaredAnnotation(Remotable.class) != null){
            remoteClasses.put(cls, Optional.of(cls));
            return cls;

        }
        for(Class<?> i : cls.getInterfaces()){
            Class<?> resCls = getRemotableClass(i);
            if(resCls != null){
                return resCls;
            }
        }
        remoteClasses.put(cls, Optional.empty());
        return null;
    }
}
