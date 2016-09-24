package com.persistentbit.substema.rod.values;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RValueNumber extends BaseValueClass implements RValue{
    public final String numberAsString;

    public RValueNumber(String numberAsString) {
        this.numberAsString = numberAsString;
    }
}
