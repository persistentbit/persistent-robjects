package com.persistentbit.substema.describe;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.NoEqual;
import com.persistentbit.jjson.mapping.description.JJPropertyDescription;
import com.persistentbit.jjson.mapping.description.JJTypeSignature;

/**
 * @author Peter Muys
 * @since 31/08/2016
 */
public class RemoteMethodDescription extends BaseValueClass{
    private final String methodName;
    private final JJTypeSignature returnType;
    private final PList<JJPropertyDescription>   parameters;
    private final boolean isCached;
    private final boolean returnsRemoteObject;
    @NoEqual  private final PList<String> doc;

    public RemoteMethodDescription(String methodName, JJTypeSignature returnType, PList<JJPropertyDescription> parameters, boolean isCached,boolean returnsRemoteObject, PList<String> doc) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameters = parameters;
        this.isCached = isCached;
        this.returnsRemoteObject = returnsRemoteObject;
        this.doc = doc;
    }

    public String getMethodName() {
        return methodName;
    }

    public JJTypeSignature getReturnType() {
        return returnType;
    }

    public PList<JJPropertyDescription> getParameters() {
        return parameters;
    }

    public boolean isCached() {
        return isCached;
    }

    public PList<String> getDoc() {
        return doc;
    }

    public boolean isReturnsRemoteObject() {
        return returnsRemoteObject;
    }
}
