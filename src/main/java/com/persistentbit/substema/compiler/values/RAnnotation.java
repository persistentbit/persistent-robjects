package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.substema.compiler.values.expr.RConst;

/**
 * Created by petermuys on 6/10/16.
 */
public class RAnnotation extends BaseValueClass {
    private final RClass    name;
    private final PMap<String,RConst> values;

    public RAnnotation(RClass name, PMap<String,RConst> values) {
        this.name = name;
        this.values = values;
    }

    public RClass getName() {
        return name;
    }
    public PMap<String,RConst> getValues() {
        return values;
    }

    public RAnnotation withName(RClass name){
        return copyWith("name",name);
    }
    public RAnnotation withValues(PMap<String,RConst> values){
        return copyWith("values",values);
    }
}
