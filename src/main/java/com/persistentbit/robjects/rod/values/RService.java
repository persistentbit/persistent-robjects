package com.persistentbit.robjects.rod.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Created by petermuys on 14/09/16.
 */
public class RService extends BaseValueClass{
    public final PList<REnum> enums;
    public final PList<RValueClass> valueClasses;
    public final PList<RRemoteClass> remoteClasses;

    public RService(PList<REnum> enums, PList<RValueClass> valueClasses, PList<RRemoteClass> remoteClasses) {
        this.enums = enums;
        this.valueClasses = valueClasses;
        this.remoteClasses = remoteClasses;
    }
}
