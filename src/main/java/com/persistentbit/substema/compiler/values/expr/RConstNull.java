package com.persistentbit.substema.compiler.values.expr;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RConstNull extends BaseValueClass implements RConst {

	static public final RConstNull Null = new RConstNull();

    private RConstNull(){

    }

	@Override
	public String toSource() {
		return "null";
	}
}
