package com.persistentbit.substema.javagen;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.StringUtils;
import com.persistentbit.substema.compiler.values.RClass;
import com.persistentbit.substema.compiler.values.RTypeSig;
import com.persistentbit.substema.compiler.values.expr.*;

import java.util.function.Consumer;

/**
 * Converts an RConst value to a java source string
 */
public class RConstToJava implements RConstVisitor<String>{
    private static int lambdaVarIndex = 1;
    private final String defaultPackageName;
    private final Consumer<RClass> imports;

    private RConstToJava(String defaultPackageName,Consumer<RClass> imports) {
        this.defaultPackageName = defaultPackageName;
        this.imports = imports;
    }

    static public String toJava(String defaultPackageName,Consumer<RClass> imports,RConst rConst){
        return new RConstToJava(defaultPackageName,imports).visit(rConst);
    }

    @Override
    public String visit(RConstBoolean c) {
        return Boolean.toString(c.isValue());
    }

    @Override
    public String visit(RConstNull c) {
        return "null";
    }

    @Override
    public String visit(RConstNumber c) {
        return c.getNumberAsString();
    }

    @Override
    public String visit(RConstEnum c) {
        imports.accept(c.getEnumClass());
        return JavaGenUtils.toString(defaultPackageName,c.getEnumClass()) + "." + c.getEnumValue();
    }

    @Override
    public String visit(RConstString c) {
        return "\"" + StringUtils.escapeToJavaString(c.getValue().substring(1,c.getValue().length()-1)) + "\"";
    }


    private void addImports(RTypeSig typeSig){
        imports.accept(typeSig.getName());
        typeSig.getGenerics().forEach(g -> addImports(g));
    }

    @Override
    public String visit(RConstValueObject c) {
        addImports(c.getTypeSig());
        //RClass.build(b -> b.setClassName("test").setPackageName("blabla"));
        String result = JavaGenUtils.toString(defaultPackageName,c.getTypeSig().getName()) + ".";
        result += JavaGenUtils.genericsToString(defaultPackageName,c.getTypeSig()).orElse("");
        String varName = "_b" + (lambdaVarIndex++);
        result += "build(" + varName + " -> " + varName;
        String setters = c.getPropValues().map(p -> "set" + StringUtils.firstCapital(p._1) + "(" + visit(p._2) + ")").toString(".");
        result += setters.isEmpty() ? "" : "." + setters;
        result += ")";

        return result;
    }

    @Override
    public String visit(RConstArray c) {
        imports.accept(JavaGenUtils.toRClass(PList.class));
        if(c.getValues().isEmpty()){
            return "PList.empty()";
        }
        return "PList.val(" + c.getValues().map(v -> visit(v)).toString(", ") + ")";
    }
}
