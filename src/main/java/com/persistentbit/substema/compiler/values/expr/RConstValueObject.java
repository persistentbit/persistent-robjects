package com.persistentbit.substema.compiler.values.expr;

import com.persistentbit.core.collections.POrderedMap;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.substema.compiler.values.RTypeSig;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RConstValueObject extends BaseValueClass implements RConst {
    private final RTypeSig typeSig;
    private final POrderedMap<String,RConst> propValues;

    public RConstValueObject(RTypeSig typeSig, POrderedMap<String,RConst> propValues) {
        this.typeSig = typeSig;
        this.propValues = propValues;
    }
    public RConstValueObject withTypeSig(RTypeSig v){
        return copyWith("typeSig",v);
    }

    public RConstValueObject withPropValues(POrderedMap<String,RConst> propValues){
        return copyWith("propValues",propValues);
    }

    public RTypeSig getTypeSig() {
        return typeSig;
    }

    public POrderedMap<String, RConst> getPropValues() {
        return propValues;
    }
}
