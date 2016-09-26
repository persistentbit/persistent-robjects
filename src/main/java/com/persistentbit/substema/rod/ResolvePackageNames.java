package com.persistentbit.substema.rod;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.substema.rod.values.RClass;
import com.persistentbit.substema.rod.values.RSubstema;

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
        throw new RuntimeException("Not Yet");
    }

    private RClass  full(RClass cls){
        if(cls.getPackageName().isEmpty() == false){
            return cls;
        }
        String clsName = cls.getClassName();
        RClass  res = resolvedNames.getOpt(clsName).orElse(null);
        if(res != null){
            return res;
        }
        res = findName(org,clsName).orElse(null);
        if(res != null){
            resolvedNames = resolvedNames.put(clsName,res);
            return res;
        }
        PList<RClass> all = PList.empty();
        for(RSubstema s : org.getImports().map(ri -> ri.getSubstema()).<RSubstema>flatten()){
            all = all.plus(findName(s,clsName).orElse(null));
        }
        all = all.filterNulls().plist();
        if(all.size()>1){
            throw new RSubstemaException("Multiple definitions for " + clsName + ": " + all.toString(","));
        }
        if(all.isEmpty()){
            throw new RSubstemaException("No  definitions found for " + clsName);
        }
        return all.head();
    }


    private Optional<RClass> findName(RSubstema stema, String className){
        PList<RClass> res = PList.empty();

        res = res.plus(stema.getInterfaceClasses().find( ic -> ic.name.getClassName().equals(className)).map(i -> i.name).orElse(null));

        res = res.plus(stema.getValueClasses().find( c -> c.typeSig.name.getClassName().equals(className)).map(i -> i.typeSig.name).orElse(null));
        res = res.plus(stema.getValueClasses().find( c -> c.typeSig.name.getClassName().equals(className)).map(i -> i.typeSig.name).orElse(null));
        res = res.filterNulls().plist();
        if(res.size() > 1){
            throw new RSubstemaException("Multiple definitions found for " + res.head());
        }
        return res.headOpt();
    }
}
