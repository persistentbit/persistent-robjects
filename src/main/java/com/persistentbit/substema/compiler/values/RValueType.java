package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.utils.BaseValueClass;


/**
 * Created by petermuys on 14/09/16.
 */
public class RValueType extends BaseValueClass {
    private final RTypeSig typeSig;
    private final boolean required;

    public RValueType(RTypeSig typeSig, boolean required) {
        this.typeSig = typeSig;
        this.required = required;
    }

    public RTypeSig getTypeSig() {
        return typeSig;
    }

    public boolean isRequired() {
        return required;
    }

    public RValueType withTypeSig(RTypeSig ts){
        return copyWith("typeSig",ts);
    }
    public RValueType withRequired(boolean r){
        return copyWith("required",r);
    }
}