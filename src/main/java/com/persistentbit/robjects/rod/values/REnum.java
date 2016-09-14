package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Created by petermuys on 14/09/16.
 */
public class REnum extends BaseValueClass {
    public final String name;
    public final PList<String> values;

    public REnum(String name, PList<String> values) {
        this.name = name;
        this.values = values;
    }
}