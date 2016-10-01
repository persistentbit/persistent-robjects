package com.persistentbit.substema.compiler.values.expr;

import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.substema.compiler.values.RClass;
import com.persistentbit.substema.compiler.values.RTypeSig;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RConstNumber extends BaseValueClass implements RConst {
    private final Number number;
    private final RClass numberType;

    public RConstNumber(RClass numberType,Number number) {
        this.number = number;
        this.numberType = numberType;
    }

    public RConstNumber withNumberAsString(String numberAsString){
        return copyWith("numberAsString",numberAsString);
    }
    public RConstNumber withNumberType(RClass numberType){
        return copyWith("numberType",numberType);
    }

    public Number getNumber() {
        return number;
    }

    public RClass getNumberType() {
        return numberType;
    }
}
