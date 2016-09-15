package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Created by petermuys on 14/09/16.
 */
public class RTypeSig extends BaseValueClass {
    public final RClass             name;
    public final PList<RTypeSig> generics;

    public RTypeSig(RClass name, PList<RTypeSig> generics) {
        this.name = name;
        this.generics = generics;
    }
}