package com.persistentbit.substema.rod.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Created by petermuys on 14/09/16.
 */
public class RSubstema extends BaseValueClass{
    private final String packageName;
    private final PList<RImport> imports;
    private final PList<REnum> enums;
    private final PList<RValueClass> valueClasses;
    private final PList<RRemoteClass> remoteClasses;
    private final PList<RInterfaceClass> interfaceClasses;

    public RSubstema(PList<RImport> imports,String packageName, PList<REnum> enums, PList<RValueClass> valueClasses, PList<RRemoteClass> remoteClasses, PList<RInterfaceClass> interfaceClasses) {
        this.imports = imports;
        this.packageName = packageName;
        this.enums = enums;
        this.valueClasses = valueClasses;
        this.remoteClasses = remoteClasses;
        this.interfaceClasses = interfaceClasses;
    }

    public String getPackageName() {
        return packageName;
    }

    public PList<RImport> getImports() {
        return imports;
    }

    public PList<REnum> getEnums() {
        return enums;
    }

    public PList<RValueClass> getValueClasses() {
        return valueClasses;
    }

    public PList<RRemoteClass> getRemoteClasses() {
        return remoteClasses;
    }

    public PList<RInterfaceClass> getInterfaceClasses() {
        return interfaceClasses;
    }

    public RSubstema    withImports(PList<RImport> imports){
        return copyWith("imports",imports);
    }
}
