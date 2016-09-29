package com.persistentbit.substema.compiler.values.expr;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.substema.compiler.values.RTypeSig;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RConstArray extends BaseValueClass implements RConst {
    private final PList<RConst>  values;
    private final RTypeSig typeSig;

    public RConstArray(RTypeSig typeSig,PList<RConst> values) {
        this.typeSig = typeSig;
        this.values = values;
    }

}
