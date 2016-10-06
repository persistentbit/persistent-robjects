package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.NoEqual;


/**
 * Created by petermuys on 14/09/16.
 */
public class RFunctionParam extends BaseValueClass {
    @NoEqual
    private final String name;
    private final RValueType valueType;
    private final PList<RAnnotation> annotations;

    public RFunctionParam(String name, RValueType valueType,PList<RAnnotation> annotations) {
        this.name = name;
        this.valueType = valueType;
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    public RValueType getValueType() {
        return valueType;
    }

    public RFunctionParam   withName(String name){
        return copyWith("name",name);
    }
    public RFunctionParam withValueType(RValueType vt){
        return copyWith("valueType",vt);
    }

    public PList<RAnnotation> getAnnotations() {
        return annotations;
    }
}