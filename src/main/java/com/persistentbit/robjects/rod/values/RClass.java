package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 15/09/2016
 */
public class RClass extends BaseValueClass{
    public final String packageName;
    public final String className;

    public RClass(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
    }
}
