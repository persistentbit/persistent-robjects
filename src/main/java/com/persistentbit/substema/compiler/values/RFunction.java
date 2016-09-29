package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

import java.util.Optional;


/**
 * Created by petermuys on 14/09/16.
 */
public class RFunction extends BaseValueClass {
    private final String name;
    private final PList<RFunctionParam> params;
    private final boolean cached;
    @Nullable  private final RValueType resultType;

    public RFunction(String name, PList<RFunctionParam> params, RValueType resultType,boolean cached) {
        this.name = name;
        this.params = params;
        this.resultType = resultType;
        this.cached = cached;
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

    public RFunction    withName(String name){
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
}