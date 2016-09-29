package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.Tuple2;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.utils.NotYet;
import com.persistentbit.substema.compiler.SubstemaException;
import com.persistentbit.substema.compiler.values.expr.*;

import java.util.Optional;
import java.util.function.Function;

/**
 *
 */
public class ResolveAndValidateConstValues implements RConstVisitor<RConst> {
    private final RTypeSig  expectedType;
    private final RSubstema substema;


    private ResolveAndValidateConstValues(RTypeSig expectedType, RSubstema substema) {
        this.expectedType = expectedType;
        this.substema = substema;
    }

    static public RConst  resolveAndValidate(RTypeSig expectedType, RConst value,RSubstema substema){
        return new ResolveAndValidateConstValues(expectedType,substema).visit(value);
    }

    @Override
    public RConst visit(RConstBoolean c) {
        if(expectedType.getName().equals(c)){
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
        throw new NotYet();
    }

    @Override
    public RConst visit(RConstEnum c) {
        return substema.getEnums()
                .find(e -> c.getEnumClass().equals(e.name) && e.values.contains(c.getEnumValue()))
                .map(e -> (RConst)c)
                .orElse(cantConvert(c));
    }

    @Override
    public RConst visit(RConstString c) {
        if(expectedType.getName().equals(stringRClass)){
            return c;
        }
        return cantConvert(c);
    }

    @Override
    public RConst visit(RConstValueObject c) {
        if(c.getTypeSig().getGenerics().isEmpty() == false){
            throw new SubstemaException("Generics are currently not supported in constant values:" + c);
        }
        RValueClass vc = substema.getValueClasses().find(v -> v.getTypeSig().getName().equals(c) && v.getTypeSig().getGenerics().isEmpty()).orElse(null);
        if(vc == null){
            throw new SubstemaException("Unknown constant value class for " + c);
        }
        boolean isInterfaceExpected = substema.getInterfaceClasses().find(i-> i.getName().equals(expectedType.getName())).isPresent();

        if(c.getTypeSig().equals(expectedType) == false){
            if(isInterfaceExpected == false){
                return cantConvert(c);
            }
            //We expect and interface and we have a value class,
            //So check if the value class implements the expected interface
            if(vc.getInterfaceClasses().contains(expectedType) == false){
                return cantConvert(c);
            }
        }
        //We now have a valid value class
        //So now check that the supplied parameters are ok
        c.withPropValues(c.getPropValues().map(pv-> {
            RTypeSig paramExpectedType = vc.getProperties().find(p -> p.getName().equals(pv._1)).map(vcp -> vcp.getValueType().getTypeSig()).orElse(null);
            if(paramExpectedType == null){
                throw new SubstemaException("Unknown property " + pv._1 + " in " + c);
            }
            RConst paramValue = ResolveAndValidateConstValues.resolveAndValidate(paramExpectedType,pv._2,substema);
            return new Tuple2<>(pv._1,paramValue);

        }).plist().groupByOneValue(t->t._1,t->t._2).po);
        vc.getProperties().map(p -> {
            p.withDefaultValue()
        })


    }

    @Override
    public RConst visit(RConstArray c) {
        return null;
    }


    private RClass findCommon(RClass left,RClass right){
        if(left.equals(right)){
            return left;
        }
        if(isNumberClass(left)){
            if(isNumberClass(right)){
                return
            } else {
                throw new Ro
            }

        }
    }
    static public final RClass stringRClass = new RClass("","String");
    static public final RClass booleanRClass = new RClass("","Boolean");
    static public final RClass listRClass = new RClass("","List");
    static public final RClass setRClass = new RClass("","Set");
    static public final RClass mapRClass = new RClass("","Map");
    static public final RClass integerRClass = new RClass("","Integer");
    static public final RClass byteRClass = new RClass("","Byte");
    static public final RClass shortRClass = new RClass("","Short");
    static public final RClass longRClass = new RClass("","Long");
    static public final RClass floatRClass = new RClass("","Float");
    static public final RClass doubleRClass = new RClass("","Double");


    static public final PSet<RClass> numberClasses = PSet.val(
            byteRClass,shortRClass,integerRClass,longRClass,
            floatRClass,doubleRClass
    );
    static public boolean isNumberClass(RClass cls){
        return numberClasses.contains(cls);
    }
}
