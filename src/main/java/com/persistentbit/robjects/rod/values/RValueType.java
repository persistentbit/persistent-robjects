package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.utils.BaseValueClass;


/**
 * Created by petermuys on 14/09/16.
 */
public class RValueType extends BaseValueClass {
    public final RTypeSig typeSig;
    public final boolean required;

    public RValueType(RTypeSig typeSig, boolean required) {
        this.typeSig = typeSig;
        this.required = required;
    }
}