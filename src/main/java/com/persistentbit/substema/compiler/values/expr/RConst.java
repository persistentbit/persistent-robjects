package com.persistentbit.substema.compiler.values.expr;

/**
 * The defintion of a Substema literal value
 * @author Peter Muys
 * @since 20/09/2016
 */
public interface RConst {

	/**
	 * Return the substema source form of this RConst
	 *
	 * @return The source String;
	 */
	String toSource();
}
