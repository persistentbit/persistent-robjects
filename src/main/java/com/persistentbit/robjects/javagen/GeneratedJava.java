package com.persistentbit.robjects.javagen;

import com.persistentbit.robjects.rod.values.RClass;

/**
 * Created by petermuys on 14/09/16.
 */
public class GeneratedJava {
    public final RClass name;
    public final String code;

    public GeneratedJava(RClass name,  String code) {
        this.name = name;
        this.code = code;
    }
}