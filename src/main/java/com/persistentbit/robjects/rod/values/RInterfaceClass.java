package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 19/09/2016
 */
public class RInterfaceClass extends BaseValueClass{
    public final RClass name;
    public final PList<RProperty> properties;

    public RInterfaceClass(RClass name, PList<RProperty> properties) {
        this.name = name;
        this.properties = properties;
    }
}
