package com.persistentbit.substema.compiler.values.expr;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RConstBoolean extends BaseValueClass implements RConst {
    private final boolean value;

    public RConstBoolean(boolean value) {
        this.value = value;
    }

    public boolean isValue() {
        return value;
	}

	@Override
	public String toSource() {
		return Boolean.toString(value);
	}
}
