package com.persistentbit.robjects.rod.values;


import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RValueString extends BaseValueClass implements RValue{
    public final String value;

    public RValueString(String value) {
        this.value = value;
    }
}
