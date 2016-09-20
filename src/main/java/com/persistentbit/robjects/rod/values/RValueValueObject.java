package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.collections.POrderedMap;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RValueValueObject extends BaseValueClass implements RValue {
    public final RClass objectClass;
    public final POrderedMap<String,RValue> propValues;

    public RValueValueObject(RClass objectClass, POrderedMap<String,RValue> propValues) {
        this.objectClass = objectClass;
        this.propValues = propValues;
    }
}
