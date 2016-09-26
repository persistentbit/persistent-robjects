package com.persistentbit.substema.rod.values;

import com.persistentbit.core.utils.BaseValueClass;

import java.util.Optional;

/**
 * Created by petermuys on 14/09/16.
 */
public class RProperty extends BaseValueClass {
    private final String     name;
    private final RValueType valueType;
    private final RValue defaultValue;

    public RProperty(String name, RValueType valueType, RValue defaultValue) {
        this.name = name;
        this.valueType = valueType;
        this.defaultValue = defaultValue;
    }
    public RProperty(String name, RValueType valueType){
        this(name,valueType,null);
    }

    public String getName() {
        return name;
    }

    public RValueType getValueType() {
        return valueType;
    }

    public Optional<RValue> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public RProperty withName(String name){
        return copyWith("name",name);
    }
    public RProperty withValueType(RValueType vt){
        return copyWith("valueType",vt);
    }
    public RProperty withDefaultValue(RValue value){
        return copyWith("defaultValue",value);
    }
}