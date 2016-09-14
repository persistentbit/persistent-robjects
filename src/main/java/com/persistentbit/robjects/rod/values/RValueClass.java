package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.robjects.rod.RodParser;

/**
 * Created by petermuys on 14/09/16.
 */
public class RValueClass extends BaseValueClass {
    public final RTypeSig typeSig;
    public final PList<RProperty> properties;

    public RValueClass(RTypeSig typeSig, PList<RProperty> properties) {
        this.typeSig = typeSig;
        this.properties = properties;
    }
}