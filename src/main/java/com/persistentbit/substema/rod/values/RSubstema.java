package com.persistentbit.substema.rod.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Created by petermuys on 14/09/16.
 */
public class RSubstema extends BaseValueClass{
    public final String packageName;
    public final PList<RImport> imports;
    public final PList<REnum> enums;
    public final PList<RValueClass> valueClasses;
    public final PList<RRemoteClass> remoteClasses;
    public final PList<RInterfaceClass> interfaceClasses;

    public RSubstema(PList<RImport> imports,String packageName, PList<REnum> enums, PList<RValueClass> valueClasses, PList<RRemoteClass> remoteClasses, PList<RInterfaceClass> interfaceClasses) {
        this.imports = imports;
        this.packageName = packageName;
        this.enums = enums;
        this.valueClasses = valueClasses;
        this.remoteClasses = remoteClasses;
        this.interfaceClasses = interfaceClasses;
    }
}
