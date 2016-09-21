package com.persistentbit.robjects;





import com.persistentbit.robjects.annotations.Remotable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * User: petermuys
 * Date: 30/10/15
 * Time: 17:23
 */
public class RemotableClasses {

    static private Map<Class<?>,Optional<Class<?>>> remoteClasses = new HashMap<>();
    static public Class<?>  getRemotableClass(Class<?> cls){
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
