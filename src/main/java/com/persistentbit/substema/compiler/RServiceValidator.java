package com.persistentbit.substema.compiler;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.substema.RObjException;
import com.persistentbit.substema.compiler.values.*;
import com.persistentbit.substema.compiler.values.expr.*;

/**
 * Created by petermuys on 17/09/16.
 */
public class RServiceValidator {
    private final RSubstema service;

    private RServiceValidator(RSubstema service) {
        this.service = service;
    }

    private void validate() {
        checkClassesDefined();
        checkOverloading();
        checkInterfaces();
    }

    private String toString(RClass rClass){
        return rClass.getPackageName() + "." + rClass.getClassName();
    }

    private void checkInterfaces() {
        PMap<RClass,RInterfaceClass> il = service.getInterfaceClasses().groupByOneValue(ic -> ic.getName());
        service.getValueClasses().forEach(vc -> {
            vc.getInterfaceClasses().forEach(icName-> {
                RInterfaceClass ic = il.getOrDefault(icName,null);
                if(ic == null){
                    throw new SubstemaException("Can't find interface " +toString(icName) + " defined in value  class " + toString(vc.getTypeSig().getName()));
                }
                PList<RProperty> notFound =ic.getProperties().filter( p -> vc.getProperties().contains(p) == false);
                if(notFound.isEmpty() == false){
                    throw new SubstemaException("Can't find properties in class " + toString(vc.getTypeSig().getName()) + " for interface " + toString(ic.getName()) + ": " + notFound.map(p -> p.getName()).toString(", "));
                }
            });
        });
    }

    private void checkOverloading() {
        PList<RClass> dup =service.getRemoteClasses().map(rc -> rc.getName())
                .plusAll(service.getValueClasses().map(vc->vc.getTypeSig().getName()))
                .plusAll(service.getEnums().map(e -> e.name))
                .plusAll(service.getInterfaceClasses().map(e->e.getName()))
                .duplicates();
        if(dup.isEmpty() == false){
            throw new SubstemaException("Duplicated type definitions: " + dup.map(c -> c.getPackageName() +"." + c.getClassName()).toString(", "));
        }
        service.getRemoteClasses().forEach(rc -> checkOverloading(rc));
        service.getValueClasses().forEach(vc -> checkOverloading(vc));
        service.getEnums().forEach(e -> checkOverloading(e));
    }
    private void checkOverloading(RRemoteClass rc){
        PList<String> dupFunNames = rc.getFunctions().map(f -> f.getName()).duplicates();
        PList<String> wrong = dupFunNames.filter(n -> rc.getFunctions().filter(f -> f.getName().equals(n)).map(f2-> f2.getParams().size()).duplicates().isEmpty() == false);
        if(wrong.isEmpty() == false){
            throw new SubstemaException("Remote class " + rc.getName().getClassName() + " has duplicated functions with the same parameter count: " + wrong.toString(", "));
        }
        rc.getFunctions().forEach(f -> checkOverloading(rc,f));
    }
    private void checkOverloading(RRemoteClass rc,RFunction f){
        PList<String> dup = f.getParams().map(p -> p.getName()).duplicates();
        if(dup.isEmpty() == false){
            throw new SubstemaException("Remote class " + rc.getName().getClassName() + " function " + f.getName() + " has duplicated parameters");
        }
    }
    private void checkOverloading(REnum e){
        PList<String> dup = e.values.duplicates();
        if(dup.isEmpty() == false){
            throw new SubstemaException("enum " + e.name.getClassName() + " has duplicated values: " + dup.toString(", "));
        }
    }


    private void checkOverloading(RValueClass vc){
        PStream<String> dup = vc.getTypeSig().getGenerics().map(sig -> sig.getName().getClassName()).duplicates();
        if(dup.isEmpty() == false){
            throw new SubstemaException("value class " + vc.getTypeSig().getName().getClassName() + " has duplicated Generics parameters: " + dup.toString(", "));
        }
        dup = vc.getProperties().map(p->p.getName()).duplicates();
        if(dup.isEmpty() == false){
            throw new SubstemaException("value class " + vc.getTypeSig().getName().getClassName() + " has duplicated property names: " + dup.toString(", "));
        }

    }



    private void checkClassesDefined(){
        PSet<RClass>   needed   =   PSet.empty();
        PSet<RClass>   defined  =   PSet.empty();
        needed = needed.plusAll(service.getValueClasses().map(vc -> needed(vc)).flatten());
        needed = needed.plusAll(service.getRemoteClasses().map(rc -> needed(rc)).flatten());
        needed = needed.plusAll(service.getInterfaceClasses().map(ic -> needed(ic)).flatten());
        defined = defined.plusAll(service.getEnums().map(e -> e.name));
        defined = defined.plusAll(service.getValueClasses().map(vc -> vc.getTypeSig().getName()));
        defined = defined.plusAll(service.getRemoteClasses().map(rc -> rc.getName()));
        defined = defined.plusAll(service.getInterfaceClasses().map(ic -> ic.getName()));
        PSet<RClass> buildIn = PSet.empty();
        buildIn = buildIn.plusAll(PSet.val("Byte","Short","Integer","Long","Float","Double","String","Boolean","List","Map","Set").map(n -> new RClass(service.getPackageName(),n)));
        PSet<RClass> all = defined.plusAll(buildIn);
        PSet<RClass> undef = needed.filter(c -> all.contains(c) == false);
        if(undef.isEmpty() == false){
            throw new SubstemaException("Following types are Undefined: " + undef.map(r -> toString(r)).toString(", "));
        }
    }


    private PSet<RClass>    needed(RInterfaceClass ic){
        PSet<RClass> res =PSet.empty();
        res =  res.plusAll(ic.getProperties().map(p -> needed(p.getValueType().getTypeSig())).flatten());
        return res;
    }

    private PSet<RClass>   needed(RRemoteClass rc){
        PSet<RClass> res = PSet.empty();
        res = res.plusAll(rc.getFunctions().map(f -> needed(f)).flatten());
        return res;
    }
    private PSet<RClass> needed(RFunction f){
        PSet<RClass> res = PSet.empty();
        if(f.getResultType().isPresent()){
            res = res.plusAll(needed(f.getResultType().get().getTypeSig()));
        }
        PStream<RClass> ap =f.getParams().map(p -> needed(p.getValueType().getTypeSig())).flatten();
        return res.plusAll(ap);
    }



    private PSet<RClass>   needed(RValueClass vc){
        PSet<RClass> res = PSet.empty();
        PSet<String> genNames = vc.getTypeSig().getGenerics().map(sig -> sig.getName().getClassName()).pset();
        return res.plusAll(vc.getProperties().map(p -> needed(p.getValueType().getTypeSig())).flatten()).filter(c -> genNames.contains(c.getClassName()) == false);
    }

    private PSet<RClass> needed(RTypeSig sig){
        PSet<RClass> res = PSet.empty();
        res = res.plus(sig.getName());
        return res.plusAll(sig.getGenerics().map(g -> needed(g)).flatten());
    }


    private boolean isEqual(RClass rcls,Class<?> jcls){
        return (rcls.getPackageName() + "." + rcls.getClassName()).equals(jcls.getName());
    }
    private boolean isAssignable(RTypeSig type, RConst value){
        if(value instanceof RConstNull){
            return true;
        }
        RClass name = type.getName();
        if(isEqual(name,String.class)){
            return value instanceof RConstString;
        }
        switch(name.getClassName()){
            case "String": return value instanceof RConstString;

            case "Byte":
            case "Short":
            case "Integer":
            case "Long":
            case "Float":
            case "Double":
                return value instanceof RConstNumber;
            case "Boolean": return value instanceof RConstBoolean;
            case "List":
            case "Set":
                return (value instanceof RConstArray) && isArrayAssignable(type,(RConstArray) value);
            case "Map":
                throw new RuntimeException("Not Yet");
        }
        if(value instanceof RConstEnum){
            RConstEnum ve = (RConstEnum) value;
            return service.getEnums().find(e -> e.name.equals(ve.enumClass) && e.values.contains(ve.enumValue) ).isPresent();
        }
        if(value instanceof RConstValueObject){
            throw new RuntimeException("Not Yet");
        }
        throw new RObjException("Unknown type:" + type.getName());
    }
    private boolean isArrayAssignable(RTypeSig type, RConstArray arrValue){
        RTypeSig itemType = type.getGenerics().head();
        return arrValue.values.find( i -> isAssignable(itemType,i) == false).isPresent() == false;
    }




    static public RSubstema validate(RSubstema service){
        new RServiceValidator(service).validate();
        return service;
    }
}
