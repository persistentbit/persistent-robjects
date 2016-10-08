package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Created by petermuys on 14/09/16.
 */
public class RSubstema extends BaseValueClass{
    private final String packageName;
    private final RPackage packageDef;
    private final PList<RImport> imports;
    private final PList<REnum> enums;
    private final PList<RValueClass> valueClasses;
    private final PList<RRemoteClass> remoteClasses;
    private final PList<RInterfaceClass> interfaceClasses;
    private final PList<RAnnotationDef> annotationDefs;

    public RSubstema(RPackage packageDef,PList<RImport> imports,String packageName, PList<REnum> enums, PList<RValueClass> valueClasses, PList<RRemoteClass> remoteClasses, PList<RInterfaceClass> interfaceClasses,PList<RAnnotationDef> annotationDefs) {
        this.packageDef = packageDef;
        this.imports = imports;
        this.packageName = packageName;
        this.enums = enums;
        this.valueClasses = valueClasses;
        this.remoteClasses = remoteClasses;
        this.interfaceClasses = interfaceClasses;
        this.annotationDefs = annotationDefs;
    }

    @Override
    public String toString() {
        return "RSubstema[" + packageName + "]";
    }

    public RPackage getPackageDef() {
        return packageDef;
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

    public PList<RAnnotationDef> getAnnotationDefs() {
        return annotationDefs;
    }

    public RSubstema    withImports(PList<RImport> imports){
        return copyWith("imports",imports);
    }
    public RSubstema    witnValueClasses(PList<RValueClass> l){
        return copyWith("valueClasses",l);
    }

}
