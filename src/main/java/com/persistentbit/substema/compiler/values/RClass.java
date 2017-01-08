package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.utils.BaseValueClass;

import java.io.File;
import java.util.Objects;

/**
 * Full name of a Substema Item.<br>
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

    public RClass(String fullClassName) {
        String packageName     = null;
        String resultClassName = fullClassName;
        int    i               = fullClassName.lastIndexOf('.');
        if(i != -1) {
            packageName = resultClassName.substring(0, i);
            resultClassName = resultClassName.substring(i + 1);
        }
        this.packageName = packageName;
        this.className = resultClassName;
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

    @Override
    public String toString() {
        return "RClass[" + getFullName() + "]";
    }

    public File getPackagePath(File rootPath) {
        String packagePath = getPackageName().replace('.', File.separatorChar);
        return new File(rootPath, packagePath);
    }
}
