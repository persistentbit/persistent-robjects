package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.utils.BaseValueClass;

import java.util.Objects;

/**
 * Full name of a Substema Itemt.<br>
 * Can be a name for a case class, remote service class, enum, interface
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
        return copyWith("packageName", n);
    }
    public RClass withClassName(String n){
        return copyWith("className",n);
    }

    public String getFullName() {
        return packageName + "." + className;
    }

}
