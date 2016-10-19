package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

import java.util.Optional;


/**
 * Contains the definition of a Substema function.<br>
 *
 * @since 14/09/16
 * @author Peter Muys
 */
public class RFunction extends BaseValueClass {
    private final String name;
    private final PList<RFunctionParam> params;
    private final boolean cached;
    @Nullable  private final RValueType resultType;
    private final PList<RAnnotation> annotations;

    public RFunction(String name, PList<RFunctionParam> params, RValueType resultType,boolean cached,PList<RAnnotation> annotations) {
        this.name = name;
        this.params = params;
        this.resultType = resultType;
        this.cached = cached;
        this.annotations = annotations;
    }


    public String getName() {
        return name;
    }

    public PList<RFunctionParam> getParams() {
        return params;
    }

    public boolean isCached() {
        return cached;
    }

    public Optional<RValueType> getResultType() {
        return Optional.ofNullable(resultType);
    }

    public PList<RAnnotation> getAnnotations() {
        return annotations;
    }

    public RFunction withName(String name){
        return copyWith("name",name);
    }
    public RFunction withParams(PList<RFunctionParam> params){
        return copyWith("params",params);
    }
    public RFunction withCached(boolean cached){
        return copyWith("cached",cached);
    }
    public RFunction withResultType(RValueType rt){
        return copyWith("resultType",rt);
    }
    public RFunction withAnnotations(PList<RAnnotation> annotations){
        return copyWith("annotations",annotations);
    }
}