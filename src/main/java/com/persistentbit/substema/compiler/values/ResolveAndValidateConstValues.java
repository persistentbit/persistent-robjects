package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.POrderedMap;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.substema.compiler.SubstemaException;
import com.persistentbit.substema.compiler.SubstemaUtils;
import com.persistentbit.substema.compiler.values.expr.*;
import com.persistentbit.substema.javagen.JavaGenUtils;

import java.util.function.Function;

/**
 *
 */
public class ResolveAndValidateConstValues implements RConstVisitor<RConst> {
    private final RTypeSig  expectedType;
    private final RSubstema substema;
    private final Function<RClass,RClass> resolveClassName;
    private final Function<RClass,REnum>  resolveEnumDef;


    private ResolveAndValidateConstValues(RTypeSig expectedType, RSubstema substema,Function<RClass,RClass> resolveClassName,Function<RClass,REnum> resolveEnumDef) {
        this.expectedType = expectedType;
        this.substema = substema;
        this.resolveClassName = resolveClassName;
        this.resolveEnumDef = resolveEnumDef;
    }

//    static public RSubstema resolveAndValidate(RSubstema substema,Function<RClass,RClass> resolveClassName){
//        PList<RValueClass> vcl = substema.getValueClasses().map( vc->
//            vc.withProperties(vc.getValues().map(p ->
//                p.withDefaultValue(p.getDefaultValue().map( dv ->
//                    resolveAndValidate(p.getValueType().getTypeSig(),dv,substema,resolveClassName)
//                    ).orElse(null)   ))
//            )
//        );
//
//        return substema.withValueClasses(vcl);
//
//    }

    @Override
    public RConst visit(RConstBoolean c) {
        if(expectedType.getName().equals(SubstemaUtils.booleanRClass)){
            return c;
        }
        return cantConvert(c);
    }

    private RConst cantConvert(RConst c){
        throw new SubstemaException("Can't convert a " + c + " to a " + expectedType);
    }

    @Override
    public RConst visit(RConstNull c) {
        return c;
    }

    @Override
    public RConst visit(RConstNumber c) {
        RClass exp = expectedType.getName();
        RClass ccls = c.getNumberType();
        if(exp.equals(c.getNumberType())){
            return c;
        }
        if(exp.equals(SubstemaUtils.longRClass) && ccls.equals(SubstemaUtils.integerRClass)){
            return new RConstNumber(exp,c.getNumber().longValue());
        }
        if(exp.equals(SubstemaUtils.floatRClass) && ccls.equals(SubstemaUtils.doubleRClass)){
            return new RConstNumber(exp,c.getNumber().floatValue());
        }
        if(exp.equals(SubstemaUtils.byteRClass)) {
            return new RConstNumber(exp, c.getNumber().byteValue());
        }
        if(exp.equals(SubstemaUtils.shortRClass)) {
            return new RConstNumber(exp, c.getNumber().shortValue());
        }
        return cantConvert(c);

    }

    @Override
    public RConst visit(RConstEnum c) {

        RClass cls = resolveClassName.apply(c.getEnumClass());
        REnum e = resolveEnumDef.apply(cls);
        if(e.getValues().contains(c.getEnumValue()) == false){
            throw new SubstemaException("Unknown contant value '" + c.getEnumValue() + "' for enum " + cls);
        }
        return c.withEnumClass(cls);
    }

    @Override
    public RConst visit(RConstString c) {
        if(expectedType.getName().equals(SubstemaUtils.stringRClass)){
            return c;
        }
        return cantConvert(c);
    }

    private RTypeSig resolveTypeSig(RTypeSig typeSig){
        typeSig = typeSig.withName(resolveClassName.apply(typeSig.getName()));
        typeSig = typeSig.withGenerics(typeSig.getGenerics().map(ts-> resolveTypeSig(ts)));
        return typeSig;
    }

    @Override
    public RConst visit(RConstValueObject c) {
        if(c.getTypeSig().getGenerics().isEmpty() == false){
            throw new SubstemaException("Generics are currently not supported in constant values:" + c);
        }
        RTypeSig resolvedTypeSig = resolveTypeSig(c.getTypeSig());
        RValueClass vc = substema.getValueClasses().find(v -> v.getTypeSig().equals(resolvedTypeSig) && v.getTypeSig().getGenerics().isEmpty()).orElseThrow(()->new SubstemaException("Unknown constant value class for " + c));

        boolean isInterfaceExpected = substema.getInterfaceClasses().find(i-> i.getName().equals(expectedType.getName())).isPresent();

        if(resolvedTypeSig.equals(expectedType) == false){
            if(isInterfaceExpected == false){
                return cantConvert(c);
            }
            //We expect and interface and we have a value class,
            //So check if the value class implements the expected interface
            if(vc.getInterfaceClasses().contains(expectedType) == false){
                return cantConvert(c);
            }
        }
        PStream<String> allNotDefinedRequiredProperties = vc.getProperties()
                .filter(p -> p.getDefaultValue().isPresent() == false && p.getValueType().isRequired())
                .map(p-> p.getName()).filterNotContainedIn(c.getPropValues().keys());
        if(allNotDefinedRequiredProperties.isEmpty() == false){
            throw new SubstemaException("Required properties missing in " + JavaGenUtils.toString(substema.getPackageName(),resolvedTypeSig) + ": " + allNotDefinedRequiredProperties.toString(", "));
        }

        PStream<String> allUnknownValues = c.getPropValues().keys().filterNotContainedIn(vc.getProperties().map(t->t.getName()));
        if(allUnknownValues.isEmpty() == false){
            throw new SubstemaException("Unknown properties in " + JavaGenUtils.toString(substema.getPackageName(),resolvedTypeSig) + ": " + allUnknownValues.toString(", "));
        }

        //We now know that the required parameters are there.
        //so now we are going to convert them to the correct type

        POrderedMap<String,RConst> converted = POrderedMap.empty();

        converted = converted.plusAll(c.getPropValues().map(t -> {
                RTypeSig expected = vc.getProperties().find(p -> p.getName().equals(t._1)).map(p -> p.getValueType().getTypeSig()).get();
                return t.with_2( resolveAndValidate(expected,t._2,substema,resolveClassName,resolveEnumDef));
        }));
        return c.withTypeSig(resolvedTypeSig).withPropValues(converted);


    }

    @Override
    public RConst visit(RConstArray c) {
        RTypeSig expected = expectedType.getGenerics().head();
        return c.withValues(c.getValues().map(v -> resolveAndValidate(expected,v,substema,resolveClassName,resolveEnumDef)));
    }

    static public RConst resolveAndValidate(RTypeSig expectedType, RConst value, RSubstema substema,
                                            Function<RClass, RClass> resolveClassName,
                                            Function<RClass, REnum> resolveEnumDef
    ) {
        return new ResolveAndValidateConstValues(expectedType, substema, resolveClassName, resolveEnumDef).visit(value);
    }



}
