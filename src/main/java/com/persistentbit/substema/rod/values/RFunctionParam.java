package com.persistentbit.substema.rod.values;

import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.NoEqual;


/**
 * Created by petermuys on 14/09/16.
 */
public class RFunctionParam extends BaseValueClass {
    @NoEqual
    public final String name;
    public final RValueType valueType;

    public RFunctionParam(String name, RValueType valueType) {
        this.name = name;
        this.valueType = valueType;
    }
}