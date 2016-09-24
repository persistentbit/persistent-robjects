package com.persistentbit.substema.rod.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Created by petermuys on 14/09/16.
 */
public class RValueClass extends BaseValueClass {
    public final RTypeSig typeSig;
    public final PList<RProperty> properties;
    public final PList<RClass> interfaceClasses;

    public RValueClass(RTypeSig typeSig, PList<RProperty> properties,PList<RClass> interfaceClasses) {
        this.typeSig = typeSig;
        this.properties = properties;
        this.interfaceClasses = interfaceClasses;
    }
}