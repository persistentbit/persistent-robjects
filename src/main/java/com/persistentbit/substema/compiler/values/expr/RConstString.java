package com.persistentbit.substema.compiler.values.expr;


import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.StringUtils;

/**
 * @author Peter Muys
 * @since 20/09/2016
 */
public class RConstString extends BaseValueClass implements RConst {
    private final String value;


    public RConstString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toSource() {
        return "\"" + StringUtils.escapeToJavaString(value) + "\"";
    }
}
