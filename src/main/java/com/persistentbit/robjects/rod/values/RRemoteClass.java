package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;


/**
 * Created by petermuys on 14/09/16.
 */
public class RRemoteClass extends BaseValueClass {
    public final RTypeSig typeSig;
    public final PList<RFunction> functions;

    public RRemoteClass(RTypeSig typeSig, PList<RFunction> functions) {
        this.typeSig = typeSig;
        this.functions = functions;
    }
}