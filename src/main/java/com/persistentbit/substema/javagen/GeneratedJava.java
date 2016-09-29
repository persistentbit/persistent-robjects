package com.persistentbit.substema.javagen;

import com.persistentbit.substema.compiler.values.RClass;

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