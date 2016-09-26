package com.persistentbit.substema.rod.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Created by petermuys on 14/09/16.
 */
public class RValueClass extends BaseValueClass {
    private final RTypeSig typeSig;
    private final PList<RProperty> properties;
    private final PList<RClass> interfaceClasses;

    public RValueClass(RTypeSig typeSig, PList<RProperty> properties,PList<RClass> interfaceClasses) {
        this.typeSig = typeSig;
        this.properties = properties;
        this.interfaceClasses = interfaceClasses;
    }

    public RTypeSig getTypeSig() {
        return typeSig;
    }

    public PList<RProperty> getProperties() {
        return properties;
    }

    public PList<RClass> getInterfaceClasses() {
        return interfaceClasses;
    }

    public RValueClass withTypeSig(RTypeSig typeSig){
        return copyWith("typeSig",typeSig);
    }

    public RValueClass withProperties(PList<RProperty> properties){
        return copyWith("properties",properties);
    }

    public RValueClass withInterfaceClasses(PList<RClass> interfaceClasses){
        return copyWith("interfaceClasses",interfaceClasses);
    }
}