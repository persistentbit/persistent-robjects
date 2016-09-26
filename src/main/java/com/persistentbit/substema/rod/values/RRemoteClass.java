package com.persistentbit.substema.rod.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;


/**
 * Created by petermuys on 14/09/16.
 */
public class RRemoteClass extends BaseValueClass {
    private final RClass name;
    private final PList<RFunction> functions;

    public RRemoteClass(RClass name, PList<RFunction> functions) {
        this.name = name;
        this.functions = functions;
    }

    public RClass getName() {
        return name;
    }

    public PList<RFunction> getFunctions() {
        return functions;
    }

    public RRemoteClass withName(RClass name){
        return copyWith("name",name);
    }
    public RRemoteClass withFunctions(PList<RFunction> functions){
        return copyWith("functions",functions);
    }
}