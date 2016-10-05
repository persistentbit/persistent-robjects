package com.persistentbit.substema.compiler;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.NotYet;
import com.persistentbit.substema.compiler.values.*;
import com.persistentbit.substema.compiler.values.expr.RConst;

import java.util.Optional;

/**
 * @author Peter Muys
 * @since 26/09/2016
 */
public class ResolvePackageNames {
    private final String packageName;
    private final RSubstema org;
    private  PMap<String,RClass>   resolvedNames = PMap.empty();

    private ResolvePackageNames(RSubstema org) {
        this.org = org;
        this.packageName = org.getPackageName();
    }

    static public RSubstema    resolve(RSubstema compiled){
        return new ResolvePackageNames(compiled).resolve();
    }

    public RSubstema resolve(){
        RSubstema res = new RSubstema(
                org.getImports(),
                org.getPackageName(),
                org.getEnums().map(ec-> resolve(ec)),
                org.getValueClasses().map(vc -> resolve(vc)),
                org.getRemoteClasses().map(rc -> resolve(rc)),
                org.getInterfaceClasses().map(ic -> resolve(ic))
        );
        return ResolveAndValidateConstValues.resolveAndValidate(res, rcls -> rcls.getPackageName().isEmpty() ? findName(res,rcls.getClassName()).get() : rcls);
    }

    private REnum   resolve(REnum ec){
        return ec;
    }
    private RValueClass resolve(RValueClass vc){
        PMap<String,RClass> backup = resolvedNames;
        resolvedNames = resolvedNames.plusAll(vc.getTypeSig().getGenerics().map(ts -> new Tuple2<String, RClass>(ts.getName().getClassName(),ts.getName().withPackageName(packageName))));
        RValueClass result = vc.withInterfaceClasses(vc.getInterfaceClasses().map(rc -> full(rc)))
                .withProperties(vc.getProperties().map(p -> resolve(p)))
                .withTypeSig(resolve(vc.getTypeSig()))
                ;
        resolvedNames = backup;
        return result;
    }
    private RTypeSig resolve(RTypeSig typeSig){
        return typeSig.withName(full(typeSig.getName())).withGenerics(typeSig.getGenerics().map(g-> resolve(g)));
    }
    private RInterfaceClass resolve(RInterfaceClass ic){
        return ic.withName(full(ic.getName())).withProperties(ic.getProperties().map(p -> resolve(p)));

    }
    private RProperty resolve(RProperty p){
        return p.withValueType(resolve(p.getValueType()));
    }
    private RValueType  resolve(RValueType vt){
        return vt.withTypeSig(resolve(vt.getTypeSig()));
    }
    private RConst resolve(RTypeSig expectedType, RConst v){
        if(v== null){
            return null;
        }

        throw new NotYet();
    }

    private RRemoteClass resolve(RRemoteClass rc){
        return rc.withName(full(rc.getName())).withFunctions(rc.getFunctions().map(f -> resolve(f)));

    }
    private RFunction resolve(RFunction f){
        return f.withParams(f.getParams().map(p -> resolve(p))).withResultType(f.getResultType().map(ff -> resolve(ff)).orElse(null));
    }

    private RFunctionParam resolve(RFunctionParam p){
        return p.withValueType(resolve(p.getValueType()));
    }

    private RClass  full(RClass cls){
        if(cls.getPackageName().isEmpty() == false){
            return cls;
        }
        String clsName = cls.getClassName();
        switch(clsName){
            case "String":
            case "Byte":
            case "Short":
            case "Integer":
            case "Long":
            case "Float":
            case "Double":
            case "Boolean":
            case "List":
            case "Set":
            case "Map":
            case "Date":
            case "DateTime":
                return cls;
        }

        RClass  res = resolvedNames.getOpt(clsName).orElse(null);
        if(res != null){
            return res;
        }
        res = findName(org,clsName).orElse(null);
        if(res != null){
            res = res.withPackageName(packageName);
            resolvedNames = resolvedNames.put(clsName,res);
            return res;
        }
        PList<RClass> all = PList.empty();
        for(RSubstema s : org.getImports().map(ri -> ri.getSubstema().orElse(null)).filterNulls()){
            all = all.plus(findName(s,clsName).orElse(null));
        }
        all = all.filterNulls().plist();
        if(all.size()>1){
            throw new SubstemaException("Multiple definitions for " + clsName + ": " + all.toString(","));
        }
        if(all.isEmpty()){
            throw new SubstemaException("No  definitions found for " + clsName);
        }
        return all.head();
    }


    private Optional<RClass> findName(RSubstema stema, String className){
        PList<RClass> res = PList.empty();

        res = res.plus(stema.getInterfaceClasses().find( ic -> ic.getName().getClassName().equals(className)).map(i -> i.getName()).orElse(null));

        res = res.plus(stema.getValueClasses().find( c -> c.getTypeSig().getName().getClassName().equals(className)).map(i -> i.getTypeSig().getName()).orElse(null));
        res = res.plus(stema.getRemoteClasses().find( c -> c.getName().getClassName().equals(className)).map(i -> i.getName()).orElse(null));

        res = res.plus(stema.getEnums().find(e -> e.getName().getClassName().equals(className)).map(i -> i.getName()).orElse(null));

        res = res.filterNulls().plist();
        if(res.size() > 1){
            throw new SubstemaException("Multiple definitions found for " + className + ": " + res.toString(","));
        }
        return res.headOpt();
    }


}
