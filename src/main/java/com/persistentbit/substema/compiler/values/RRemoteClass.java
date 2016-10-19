package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;


/**
 * Contains the definition of a Substema Remotable class.<br>
 * @since 14/09/16
 * @author Peter Muys
 */
public class RRemoteClass extends BaseValueClass {
    private final RClass name;
    private final PList<RFunction> functions;
    private final PList<RAnnotation> annotations;

    public RRemoteClass(RClass name, PList<RFunction> functions,PList<RAnnotation> annotations) {
        this.name = name;
        this.functions = functions;
        this.annotations = annotations;
    }

    public RClass getName() {
        return name;
    }

    public PList<RFunction> getFunctions() {
        return functions;
    }

    public PList<RAnnotation> getAnnotations() {
        return annotations;
    }

    public RRemoteClass withName(RClass name){
        return copyWith("name",name);
    }
    public RRemoteClass withFunctions(PList<RFunction> functions){
        return copyWith("functions",functions);
    }
    public RRemoteClass withAnnotations(PList<RAnnotation> annotations){
        return copyWith("annotations",annotations);
    }
}