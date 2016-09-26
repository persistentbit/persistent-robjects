package com.persistentbit.substema.rod.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Created by petermuys on 14/09/16.
 */
public class RTypeSig extends BaseValueClass {
    private final RClass             name;
    private final PList<RTypeSig> generics;

    public RTypeSig(RClass name, PList<RTypeSig> generics) {
        this.name = name;
        this.generics = generics;
    }

    public RClass getName() {
        return name;
    }

    public PList<RTypeSig> getGenerics() {
        return generics;
    }

    public RTypeSig withName(RClass name){
        return copyWith("name",name);
    }
    public RTypeSig withGenerics(PList<RTypeSig> generics){
        return copyWith("generics",generics);
    }
}