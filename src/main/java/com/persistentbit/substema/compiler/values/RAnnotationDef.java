package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Created by petermuys on 6/10/16.
 */
public class RAnnotationDef extends BaseValueClass {
    private final RClass    name;
    private final PList<RProperty> properties;

    public RAnnotationDef(RClass name, PList<RProperty> properties) {
        this.name = name;
        this.properties = properties;
    }

    public PList<RProperty> getProperties() {
        return properties;
    }


    public RClass getName() {
        return name;
    }
}