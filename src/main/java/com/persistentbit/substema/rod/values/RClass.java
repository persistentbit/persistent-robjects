package com.persistentbit.substema.rod.values;

import com.persistentbit.core.utils.BaseValueClass;

import java.util.Objects;

/**
 * @author Peter Muys
 * @since 15/09/2016
 */
public class RClass extends BaseValueClass{
    private final String packageName;
    private final String className;

    public RClass(String packageName, String className) {
        this.packageName = Objects.requireNonNull(packageName);
        this.className = Objects.requireNonNull(className);
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public RClass withPackageName(String n){
        return new RClass(n,className);
    }
    public RClass withClassName(String n){
        return new RClass(packageName,className);
    }
}
