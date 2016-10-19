package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Contains the definition of a Substema Annotation
 * @since 6/10/16
 * @author Peter Muys
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

    public RAnnotationDef withName(RClass name){
        return copyWith("name",name);
    }

    public RAnnotationDef withProperties(PList<RProperty> properties){
        return copyWith("properties", properties);
    }
}