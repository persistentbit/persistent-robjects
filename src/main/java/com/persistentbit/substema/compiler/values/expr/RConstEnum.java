package com.persistentbit.substema.compiler.values.expr;

import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.substema.compiler.values.RClass;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RConstEnum extends BaseValueClass implements RConst {
    private final RClass enumClass;
    private final String  enumValue;

    public RConstEnum(RClass enumClass, String enumValue) {
        this.enumClass = enumClass;
        this.enumValue = enumValue;
    }


    public RClass getEnumClass() {
        return enumClass;
    }

    public String getEnumValue() {
        return enumValue;
    }

    public RConstEnum withEnumClass(RClass cls){
        return copyWith("enumClass",cls);
    }
    public RConstEnum withEnumValue(String value){
        return copyWith("enumValue",value);
    }

	@Override
	public String toSource() {
		return enumClass.getClassName() + "." + enumValue;
	}
}
