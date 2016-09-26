package com.persistentbit.substema.rod;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.substema.RObjException;
import com.persistentbit.substema.rod.values.*;

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
        PMap<RClass,RInterfaceClass> il = service.getInterfaceClasses().groupByOneValue(ic -> ic.name);
        service.getValueClasses().forEach(vc -> {
            vc.interfaceClasses.forEach(icName-> {
                RInterfaceClass ic = il.getOrDefault(icName,null);
                if(ic == null){
                    throw new RSubstemaException("Can't find interface " +toString(icName) + " defined in value  class " + toString(vc.typeSig.name));
                }
                PList<RProperty> notFound =ic.properties.filter( p -> vc.properties.contains(p) == false);
                if(notFound.isEmpty() == false){
                    throw new RSubstemaException("Can't find properties in class " + toString(vc.typeSig.name) + " for interface " + toString(ic.name) + ": " + notFound.map(p -> p.name).toString(", "));
                }
            });
        });
    }

    private void checkOverloading() {
        PList<RClass> dup =service.getRemoteClasses().map(rc -> rc.name)
                .plusAll(service.getValueClasses().map(vc->vc.typeSig.name))
                .plusAll(service.getEnums().map(e -> e.name))
                .plusAll(service.getInterfaceClasses().map(e->e.name))
                .duplicates();
        if(dup.isEmpty() == false){
            throw new RSubstemaException("Duplicated type definitions: " + dup.map(c -> c.getPackageName() +"." + c.getClassName()).toString(", "));
        }
        service.getRemoteClasses().forEach(rc -> checkOverloading(rc));
        service.getValueClasses().forEach(vc -> checkOverloading(vc));
        service.getEnums().forEach(e -> checkOverloading(e));
    }
    private void checkOverloading(RRemoteClass rc){
        PList<String> dupFunNames = rc.functions.map(f -> f.name).duplicates();
        PList<String> wrong = dupFunNames.filter(n -> rc.functions.filter(f -> f.name.equals(n)).map(f2-> f2.params.size()).duplicates().isEmpty() == false);
        if(wrong.isEmpty() == false){
            throw new RSubstemaException("Remote class " + rc.name.getClassName() + " has duplicated functions with the same parameter count: " + wrong.toString(", "));
        }
        rc.functions.forEach(f -> checkOverloading(rc,f));
    }
    private void checkOverloading(RRemoteClass rc,RFunction f){
        PList<String> dup = f.params.map(p -> p.name).duplicates();
        if(dup.isEmpty() == false){
            throw new RSubstemaException("Remote class " + rc.name.getClassName() + " function " + f.name + " has duplicated parameters");
        }
    }
    private void checkOverloading(REnum e){
        PList<String> dup = e.values.duplicates();
        if(dup.isEmpty() == false){
            throw new RSubstemaException("enum " + e.name.getClassName() + " has duplicated values: " + dup.toString(", "));
        }
    }


    private void checkOverloading(RValueClass vc){
        PStream<String> dup = vc.typeSig.generics.map(sig -> sig.name.getClassName()).duplicates();
        if(dup.isEmpty() == false){
            throw new RSubstemaException("value class " + vc.typeSig.name.getClassName() + " has duplicated Generics parameters: " + dup.toString(", "));
        }
        dup = vc.properties.map(p->p.name).duplicates();
        if(dup.isEmpty() == false){
            throw new RSubstemaException("value class " + vc.typeSig.name.getClassName() + " has duplicated property names: " + dup.toString(", "));
        }

    }



    private void checkClassesDefined(){
        PSet<RClass>   needed   =   PSet.empty();
        PSet<RClass>   defined  =   PSet.empty();
        needed = needed.plusAll(service.getValueClasses().map(vc -> needed(vc)).flatten());
        needed = needed.plusAll(service.getRemoteClasses().map(rc -> needed(rc)).flatten());
        needed = needed.plusAll(service.getInterfaceClasses().map(ic -> needed(ic)).flatten());
        defined = defined.plusAll(service.getEnums().map(e -> e.name));
        defined = defined.plusAll(service.getValueClasses().map(vc -> vc.typeSig.name));
        defined = defined.plusAll(service.getRemoteClasses().map(rc -> rc.name));
        defined = defined.plusAll(service.getInterfaceClasses().map(ic -> ic.name));
        PSet<RClass> buildIn = PSet.empty();
        buildIn = buildIn.plusAll(PSet.val("Byte","Short","Integer","Long","Float","Double","String","Boolean","List","Map","Set").map(n -> new RClass(service.getPackageName(),n)));
        PSet<RClass> all = defined.plusAll(buildIn);
        PSet<RClass> undef = needed.filter(c -> all.contains(c) == false);
        if(undef.isEmpty() == false){
            throw new RSubstemaException("Following types are Undefined: " + undef.map(r -> toString(r)).toString(", "));
        }
    }


    private PSet<RClass>    needed(RInterfaceClass ic){
        PSet<RClass> res =PSet.empty();
        res =  res.plusAll(ic.properties.map(p -> needed(p.valueType.typeSig)).flatten());
        return res;
    }

    private PSet<RClass>   needed(RRemoteClass rc){
        PSet<RClass> res = PSet.empty();
        res = res.plusAll(rc.functions.map(f -> needed(f)).flatten());
        return res;
    }
    private PSet<RClass> needed(RFunction f){
        PSet<RClass> res = PSet.empty();
        if(f.resultType != null){
            res = res.plusAll(needed(f.resultType.typeSig));
        }
        PStream<RClass> ap =f.params.map(p -> needed(p.valueType.typeSig)).flatten();
        return res.plusAll(ap);
    }



    private PSet<RClass>   needed(RValueClass vc){
        PSet<RClass> res = PSet.empty();
        PSet<String> genNames = vc.typeSig.generics.map(sig -> sig.name.getClassName()).pset();
        return res.plusAll(vc.properties.map(p -> needed(p.valueType.typeSig)).flatten()).filter(c -> genNames.contains(c.getClassName()) == false);
    }

    private PSet<RClass> needed(RTypeSig sig){
        PSet<RClass> res = PSet.empty();
        res = res.plus(sig.name);
        return res.plusAll(sig.generics.map(g -> needed(g)).flatten());
    }


    private boolean isEqual(RClass rcls,Class<?> jcls){
        return (rcls.getPackageName() + "." + rcls.getClassName()).equals(jcls.getName());
    }
    private boolean isAssignable(RTypeSig type, RValue value){
        if(value instanceof RValueNull){
            return true;
        }
        RClass name = type.name;
        if(isEqual(name,String.class)){
            return value instanceof RValueString;
        }
        switch(name.getClassName()){
            case "String": return value instanceof RValueString;

            case "Byte":
            case "Short":
            case "Integer":
            case "Long":
            case "Float":
            case "Double":
                return value instanceof  RValueNumber;
            case "Boolean": return value instanceof RValueBoolean;
            case "List":
            case "Set":
                return (value instanceof RValueArray) && isArrayAssignable(type,(RValueArray) value);
            case "Map":
                throw new RuntimeException("Not Yet");
        }
        if(value instanceof RValueEnum){
            RValueEnum ve = (RValueEnum) value;
            return service.getEnums().find(e -> e.name.equals(ve.enumClass) && e.values.contains(ve.enumValue) ).isPresent();
        }
        if(value instanceof RValueValueObject){
            throw new RuntimeException("Not Yet");
        }
        throw new RObjException("Unknown type:" + type.name);
    }
    private boolean isArrayAssignable(RTypeSig type, RValueArray arrValue){
        RTypeSig itemType = type.generics.head();
        return arrValue.values.find( i -> isAssignable(itemType,i) == false).isPresent() == false;
    }




    static public RSubstema validate(RSubstema service){
        new RServiceValidator(service).validate();
        return service;
    }
}
