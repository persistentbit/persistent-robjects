package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Contains the definition of a Substema Interface class.<br>
 * @author Peter Muys
 * @since 19/09/2016
 */
public class RInterfaceClass extends BaseValueClass{
    private final RClass name;
    private final PList<RProperty> properties;
    private final PList<RAnnotation> annotations;

    public RInterfaceClass(RClass name, PList<RProperty> properties,PList<RAnnotation> annotations) {
        this.name = name;
        this.properties = properties;
        this.annotations = annotations;
    }

    public RClass getName() {
        return name;
    }
    public PList<RProperty> getProperties() {
        return properties;
    }
    public PList<RAnnotation> getAnnotations() {
        return annotations;
    }

    public RInterfaceClass withName(RClass name){
        return copyWith("name",name);
    }
    public RInterfaceClass withProperties(PList<RProperty> properties){
        return copyWith("properties",properties);
    }
    public RInterfaceClass withAnnotations(PList<RAnnotation> annotations){
        return copyWith("annotations",annotations);
    }

}
