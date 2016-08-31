package com.persistentbit.robjects.describe;

import com.persistentbit.core.collections.PList;
import com.persistentbit.jjson.mapping.description.JJPropertyDescription;
import com.persistentbit.jjson.mapping.description.JJTypeSignature;

/**
 * @author Peter Muys
 * @since 31/08/2016
 */
public class RemoteMethodDescription {
    private final String methodName;
    private final JJTypeSignature returnType;
    private final PList<JJPropertyDescription>   parameters;
    private final boolean isCached;
    private final PList<String> doc;

    public RemoteMethodDescription(String methodName, JJTypeSignature returnType, PList<JJPropertyDescription> parameters, boolean isCached, PList<String> doc) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameters = parameters;
        this.isCached = isCached;
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
}
