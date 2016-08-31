package com.persistentbit.robjects.describe;

import com.persistentbit.core.collections.PList;
import com.persistentbit.jjson.mapping.description.JJTypeSignature;

/**
 * @author Peter Muys
 * @since 31/08/2016
 */
public class RemoteClassDescription {
    private final JJTypeSignature type;
    private final PList<RemoteMethodDescription>    methods;

    public RemoteClassDescription(JJTypeSignature type, PList<RemoteMethodDescription> methods) {
        this.type = type;
        this.methods = methods;
    }

}
