package com.persistentbit.substema.compiler;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.ToDo;
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
    private final SubstemaCompiler compiler;
    private  PMap<String,RClass>   resolvedNames = PMap.empty();


    private ResolvePackageNames(SubstemaCompiler compiler,RSubstema org) {
        this.org = org;
        this.packageName = org.getPackageName();
        this.compiler = compiler;

    }

    static public RSubstema    resolve(SubstemaCompiler compiler,RSubstema compiled){
        return new ResolvePackageNames(compiler,compiled).resolve();
    }

    public RSubstema resolve(){
        RSubstema res = new RSubstema(
                resolve(org.getPackageDef()),
                org.getImports(),
                org.getPackageName(),
                org.getEnums().map(ec-> resolve(ec)),
                org.getValueClasses().map(vc -> resolve(vc)),
                org.getRemoteClasses().map(rc -> resolve(rc)),
                org.getInterfaceClasses().map(ic -> resolve(ic)),
                org.getAnnotationDefs().map(ad-> resolve(ad))
        );
        return ResolveAndValidateConstValues.resolveAndValidate(res,
                rcls -> rcls.getPackageName().isEmpty()
                        ? findName(res,rcls.getClassName()).get() : rcls);
    }

    private RAnnotationDef resolve(RAnnotationDef ad){
        return ad;
    }

    private RPackage resolve(RPackage packageDef){
        return new RPackage(resolve(packageDef.getAnnotations()));
    }

    private REnum   resolve(REnum ec){
        return ec
                .withAnnotations(resolve(ec.getAnnotations()))
                .withName(full(ec.getName()));
    }
    private RValueClass resolve(RValueClass vc){
        PMap<String,RClass> backup = resolvedNames;
        resolvedNames = resolvedNames.plusAll(
                vc.getTypeSig().getGenerics().map(
                        ts -> new Tuple2<>(ts.getName().getClassName(),ts.getName().withPackageName(packageName)
                        )
                )
        );
        RValueClass result = vc.withInterfaceClasses(vc.getInterfaceClasses().map(rc -> full(rc)))
                .withProperties(vc.getProperties().map(p -> resolve(p)))
                .withTypeSig(resolve(vc.getTypeSig()))
                .withAnnotations(resolve(vc.getAnnotations()))
                ;
        resolvedNames = backup;
        return result;
    }

    private PList<RAnnotation> resolve(PList<RAnnotation> annotations){
        return annotations.map(an -> an.withName(full(an.getName())));
    }


    private RTypeSig resolve(RTypeSig typeSig){
        return typeSig.withName(full(typeSig.getName())).withGenerics(typeSig.getGenerics().map(g-> resolve(g)));
    }
    private RInterfaceClass resolve(RInterfaceClass ic){
        return ic.withName(full(ic.getName())).withProperties(ic.getProperties().map(p -> resolve(p)));

    }
    private RProperty resolve(RProperty p){
        return p
                .withValueType(resolve(p.getValueType()))
                .withAnnotations(resolve(p.getAnnotations()))
                .withDefaultValue(p.getDefaultValue().map(dv-> resolve(dv)).orElse(null))
                ;
    }

    private RConst  resolve(RConst cv){
        throw new ToDo(cv.toString());
    }

    private RValueType  resolve(RValueType vt){
        return vt.withTypeSig(resolve(vt.getTypeSig()));
    }
    private RConst resolve(RTypeSig expectedType, RConst v){
        if(v== null){
            return null;
        }

        throw new ToDo();
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
        if (SubstemaUtils.isSubstemaClass(cls)) {
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
            RSubstema implicitAnnotations = compiler.compile(SubstemaUtils.annotationsPackage);
            all = all.plus(findName(implicitAnnotations,clsName).orElse(null));
            all = all.filterNulls().plist();
        }
        if(all.isEmpty()){
            throw new SubstemaException("No  definitions found for " + cls);
        }
        return all.head();
    }


    private Optional<RClass> findName(RSubstema stema, String className){
        PList<RClass> res = PList.empty();

        res = res.plus(stema.getInterfaceClasses().find( ic -> ic.getName().getClassName().equals(className)).map(i -> i.getName()).orElse(null));

        res = res.plus(stema.getValueClasses().find( c -> c.getTypeSig().getName().getClassName().equals(className)).map(i -> i.getTypeSig().getName()).orElse(null));
        res = res.plus(stema.getRemoteClasses().find( c -> c.getName().getClassName().equals(className)).map(i -> i.getName()).orElse(null));

        res = res.plus(stema.getEnums().find(e -> e.getName().getClassName().equals(className)).map(i -> i.getName()).orElse(null));

        res = res.plus(stema.getAnnotationDefs().find(e -> e.getName().getClassName().equals(className)).map(i -> i.getName()).orElse(null));

        res = res.filterNulls().plist();
        if(res.size() > 1){
            throw new SubstemaException("Multiple definitions found for " + className + ": " + res.toString(","));
        }
        return res.headOpt();
    }


}
