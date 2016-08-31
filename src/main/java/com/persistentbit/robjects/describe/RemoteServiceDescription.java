package com.persistentbit.robjects.describe;

import com.persistentbit.core.collections.PSet;
import com.persistentbit.jjson.mapping.description.JJTypeDescription;

/**
 * @author Peter Muys
 * @since 31/08/2016
 */
public class RemoteServiceDescription {
    private final RemoteClassDescription    root;
    private final PSet<RemoteClassDescription> remoteObjects;
    private final PSet<JJTypeDescription>   valueObjects;

    public RemoteServiceDescription(RemoteClassDescription root, PSet<RemoteClassDescription> remoteObjects, PSet<JJTypeDescription> valueObjects) {
        this.root = root;
        this.remoteObjects = remoteObjects;
        this.valueObjects = valueObjects;
    }

    public RemoteClassDescription getRoot() {
        return root;
    }

    public PSet<RemoteClassDescription> getRemoteObjects() {
        return remoteObjects;
    }

    public PSet<JJTypeDescription> getValueObjects() {
        return valueObjects;
    }
}
