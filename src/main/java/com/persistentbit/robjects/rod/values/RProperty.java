package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.utils.BaseValueClass;

import java.util.Optional;

/**
 * Created by petermuys on 14/09/16.
 */
public class RProperty extends BaseValueClass {
    public final String     name;
    public final RValueType valueType;
    public final Optional<RValue> defaultValue;

    public RProperty(String name, RValueType valueType, RValue defaultValue) {
        this.name = name;
        this.valueType = valueType;
        this.defaultValue = Optional.ofNullable(defaultValue);
    }
    public RProperty(String name, RValueType valueType){
        this(name,valueType,null);
    }
}