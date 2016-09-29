package com.persistentbit.substema.compiler.values.expr;

import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.substema.compiler.values.RTypeSig;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RConstBoolean extends BaseValueClass implements RConst {
    public final boolean value;

    public RConstBoolean(boolean value) {
        this.value = value;
    }


}
