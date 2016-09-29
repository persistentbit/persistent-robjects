package com.persistentbit.substema.compiler.values.expr;

import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.substema.compiler.values.RClass;
import com.persistentbit.substema.compiler.values.RTypeSig;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RConstNumber extends BaseValueClass implements RConst {
    private final String numberAsString;
    private final RClass numberType;

    public RConstNumber(RClass numberType,String numberAsString) {
        this.numberAsString = numberAsString;
        this.numberType = numberType;
    }

    public RConstNumber withNumberAsString(String numberAsString){
        return copyWith("numberAsString",numberAsString);
    }
    public RConstNumber withNumberType(RClass numberType){
        return copyWith("numberType",numberType);
    }


}
