package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RValueEnum extends BaseValueClass implements RValue{
    public final RClass  enumClass;
    public final String  enumValue;

    public RValueEnum(RClass enumClass, String enumValue) {
        this.enumClass = enumClass;
        this.enumValue = enumValue;
    }
}
