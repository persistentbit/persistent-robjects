package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 19/09/2016
 */
public class RInterfaceClass extends BaseValueClass{
    private final RClass name;
    private final PList<RProperty> properties;

    public RInterfaceClass(RClass name, PList<RProperty> properties) {
        this.name = name;
        this.properties = properties;
    }

    public RClass getName() {
        return name;
    }

    public PList<RProperty> getProperties() {
        return properties;
    }

    public RInterfaceClass withName(RClass name){
        return copyWith("name",name);
    }
    public RInterfaceClass withProperties(PList<RProperty> properties){
        return copyWith("properties",properties);
    }
}
