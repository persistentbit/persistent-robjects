package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RValueArray extends BaseValueClass implements RValue {
    public final PList<RValue>  values;

    public RValueArray(PList<RValue> values) {
        this.values = values;
    }
}
