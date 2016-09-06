package com.persistentbit.robjects.describe;

import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.jjson.mapping.description.JJTypeDescription;
import com.persistentbit.jjson.mapping.description.JJTypeSignature;

/**
 * @author Peter Muys
 * @since 31/08/2016
 */
public class RemoteServiceDescription extends BaseValueClass{
    private final JJTypeSignature rootSignature;
    private final PSet<RemoteClassDescription> remoteObjects;
    private final PSet<JJTypeDescription>   valueObjects;

    public RemoteServiceDescription(JJTypeSignature rootSignature, PSet<RemoteClassDescription> remoteObjects, PSet<JJTypeDescription> valueObjects) {
        this.rootSignature = rootSignature;
        this.remoteObjects = remoteObjects;
        this.valueObjects = valueObjects;
    }

    public JJTypeSignature getRootSignature() {
        return rootSignature;
    }

    public PSet<RemoteClassDescription> getRemoteObjects() {
        return remoteObjects;
    }

    public PSet<JJTypeDescription> getValueObjects() {
        return valueObjects;
    }
}
