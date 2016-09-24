package com.persistentbit.substema;

import com.persistentbit.core.collections.PMap;

import java.lang.reflect.Method;

/**
 * @author Peter Muys
 * @since 2/09/2016
 */
public class RemotableMethods {

    static private PMap<MethodDefinition,Method> remoteMethods = PMap.empty();
    static public Method  getRemotableMethod(MethodDefinition md){
        Method res = remoteMethods.get(md);
        if(res != null){
            return res;
        }
        try {
            res = md.getRemotableClass().getDeclaredMethod(md.getMethodName(),md.getParamTypes());
            remoteMethods = remoteMethods.put(md,res);
            return res;
        } catch (NoSuchMethodException e) {
            throw new RObjException("Can't find method in " + md.getRemotableClass().getName() + ":" + md);
        }
    }
}
