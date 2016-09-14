package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;


/**
 * Created by petermuys on 14/09/16.
 */
public class RFunction extends BaseValueClass {
    public final String name;
    public final PList<RFunctionParam> params;
    @Nullable  public final RValueType resultType;

    public RFunction(String name, PList<RFunctionParam> params, RValueType resultType) {
        this.name = name;
        this.params = params;
        this.resultType = resultType;
    }
}