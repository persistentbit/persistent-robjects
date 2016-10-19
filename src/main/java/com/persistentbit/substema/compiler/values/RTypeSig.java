package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * This class contains the full type signature of value, meaning the {@link RClass} and the {@link RTypeSig}
 * of the Generic parameters.<br>
 *
 * @since 14/09/16
 * @author Peter Muys
 */
public class RTypeSig extends BaseValueClass {
    private final RClass             name;
    private final PList<RTypeSig> generics;

    public RTypeSig(RClass name, PList<RTypeSig> generics) {
        this.name = name;
        this.generics = generics;
    }
    public RTypeSig(RClass name){
        this(name,PList.empty());
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