package com.persistentbit.substema.javagen;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * Created by petermuys on 14/09/16.
 */
public class JavaGenOptions extends BaseValueClass{
    public final boolean generateGetters;
    public final boolean generateUpdaters;

    public JavaGenOptions(boolean generateGetters, boolean generateUpdaters) {
        this.generateGetters = generateGetters;
        this.generateUpdaters = generateUpdaters;
    }

    public JavaGenOptions() {
        this(false,false);
    }

    public JavaGenOptions withGenerateGetters(boolean generateGetters){
        return copyWith("generateGetters",generateGetters);
    }
    public JavaGenOptions withGenerateUpdaters(boolean generateUpdaters){
        return copyWith("generateUpdaters",generateUpdaters);
    }
}
