package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RValueBoolean extends BaseValueClass implements RValue{
    public final boolean value;

    public RValueBoolean(boolean value) {
        this.value = value;
    }
}
