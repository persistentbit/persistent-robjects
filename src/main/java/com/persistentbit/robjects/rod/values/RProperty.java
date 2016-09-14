package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.robjects.rod.RodParser;

/**
 * Created by petermuys on 14/09/16.
 */
public class RProperty extends BaseValueClass {
    public final String     name;
    public final RValueType valueType;

    public RProperty(String name,RValueType valueType) {
        this.name = name;
        this.valueType = valueType;
    }
}