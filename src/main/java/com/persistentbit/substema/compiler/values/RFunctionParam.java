package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.NoEqual;


/**
 * Contains the definition of a {@link RFunction} parameter
 * @since 14/09/16
 * @author Peter Muys
 * @see RFunction
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
    public PList<RAnnotation> getAnnotations() {
        return annotations;
    }

    public RFunctionParam withName(String name){
        return copyWith("name",name);
    }
    public RFunctionParam withValueType(RValueType vt){
        return copyWith("valueType",vt);
    }
    public RFunctionParam withAnnotations(PList<RAnnotation> annotations){
        return copyWith("annotations",annotations);
    }
}