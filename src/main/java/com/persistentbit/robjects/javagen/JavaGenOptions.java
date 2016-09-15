package com.persistentbit.robjects.javagen;

/**
 * Created by petermuys on 14/09/16.
 */
public class JavaGenOptions {
    public final boolean generateGetters;
    public final boolean generateUpdaters;

    public JavaGenOptions(boolean generateGetters, boolean generateUpdaters) {
        this.generateGetters = generateGetters;
        this.generateUpdaters = generateUpdaters;
    }

    public JavaGenOptions() {
        this(true,true);
    }
}
