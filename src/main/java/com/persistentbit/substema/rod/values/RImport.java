package com.persistentbit.substema.rod.values;

import com.persistentbit.core.utils.BaseValueClass;

import java.util.Optional;

/**
 * Created by petermuys on 25/09/16.
 */
public class RImport extends BaseValueClass{
    private final String packageName;
    private final RSubstema   substema;

    public RImport(String packageName){
        this.packageName = packageName;
        this.substema = null;
    }

    public RImport(String packageName, RSubstema substema) {
        this.packageName = packageName;
        this.substema = substema;
    }

    public String getPackageName() {
        return packageName;
    }

    public Optional<RSubstema> getSubstema() {
        return Optional.ofNullable(substema);
    }

    public RImport withSubstema(RSubstema substema){
        return new RImport(packageName,substema);
    }
}
