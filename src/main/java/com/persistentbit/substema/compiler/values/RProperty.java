package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.substema.compiler.values.expr.RConst;

import java.util.Optional;

/**
 * Created by petermuys on 14/09/16.
 */
public class RProperty extends BaseValueClass {
    private final String     name;
    private final RValueType valueType;
    private final RConst defaultValue;

    public RProperty(String name, RValueType valueType, RConst defaultValue) {
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

    public Optional<RConst> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public RProperty withName(String name){
        return copyWith("name",name);
    }
    public RProperty withValueType(RValueType vt){
        return copyWith("valueType",vt);
    }
    public RProperty withDefaultValue(RConst value){
        return copyWith("defaultValue",value);
    }
}