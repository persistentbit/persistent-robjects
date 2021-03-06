package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Contains the definition of a Substema enum.<br>
 * @since 14/09/16
 * @author Peter Muys
 */
public class REnum extends BaseValueClass {
    private final RClass name;
    private final PList<String> values;
    private final PList<RAnnotation> annotations;

    public REnum(RClass name, PList<String> values,PList<RAnnotation> annotations) {
        this.name = name;
        this.values = values;
        this.annotations = annotations;
    }

    public RClass getName() {
        return name;
    }

    public PList<String> getValues() {
        return values;
    }

    public PList<RAnnotation> getAnnotations() {
        return annotations;
    }

    public REnum withAnnotations(PList<RAnnotation> annotations){
        return copyWith("annotations",annotations);
    }
    public REnum withName(RClass name){
        return copyWith("name",name);
    }

}