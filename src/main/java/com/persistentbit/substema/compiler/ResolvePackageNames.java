package com.persistentbit.substema.compiler;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.ToDo;
import com.persistentbit.substema.compiler.values.*;
import com.persistentbit.substema.compiler.values.expr.RConst;

import java.util.Optional;

/**
 * After parsing,
 * All classes ({@link RClass}) have no packages.<br>
 * This class resolves those class packages.<br>
 *
 *
 * @author Peter Muys
 * @since 26/09/2016
 */
public class ResolvePackageNames {

    private final String packageName;
    private final RSubstema originalSubstema;
    private final SubstemaCompiler compiler;
    private  PMap<String,RClass>   resolvedNames = PMap.empty();


    private ResolvePackageNames(SubstemaCompiler compiler,RSubstema originalSubstema) {
        this.originalSubstema = originalSubstema;
        this.packageName = originalSubstema.getPackageName();
        this.compiler = compiler;

    }

    /**
     * Main entry point for this class.<br>
     * @param compiler
     * @param compiled
     * @return
     */
    static public RSubstema    resolve(SubstemaCompiler compiler,RSubstema compiled){
        return new ResolvePackageNames(compiler,compiled).resolve();
    }

    public RSubstema resolve(){

        RSubstema res = new RSubstema(
                resolvePackageDef(originalSubstema.getPackageDef()),
                originalSubstema.getImports(),
                originalSubstema.getPackageName(),
                originalSubstema.getEnums().map(this::resolveEnumDef),
                originalSubstema.getValueClasses().map(this::resolveValueClass),
                originalSubstema.getRemoteClasses().map(this::resolveRemoteClassDef),
                originalSubstema.getInterfaceClasses().map(this::resolveInterfaceClass),
                originalSubstema.getAnnotationDefs().map(this::resolveAnnotationDef)
        );
//        return ResolveAndValidateConstValues.resolveAndValidate(res,
//                rcls -> rcls.getPackageName().isEmpty()
//                        ? findName(res,rcls.getClassName()).get() : rcls);
        return res;
    }

    private RAnnotationDef resolveAnnotationDef(RAnnotationDef ad){
        return ad.withProperties(
            ad.getProperties().map(this::resolveProperty)
        );
    }

    private RPackage resolvePackageDef(RPackage packageDef){
        return new RPackage(resolveAnnotations(packageDef.getAnnotations()));
    }

    private REnum resolveEnumDef(REnum ec){
        return ec.withAnnotations(resolveAnnotations(ec.getAnnotations()));
    }
    private RValueClass resolveValueClass(RValueClass vc){
        PMap<String,RClass> backup = resolvedNames;
        resolvedNames = resolvedNames.plusAll(
                vc.getTypeSig().getGenerics().map(
                        ts -> new Tuple2<>(ts.getName().getClassName(),ts.getName().withPackageName(packageName)
                        )
                )
        );
        RValueClass result = vc
                .withInterfaceClasses(vc.getInterfaceClasses().map(this::resolveClass))
                .withProperties(vc.getProperties().map(this::resolveProperty))
                .withTypeSig(resolveTypeSig(vc.getTypeSig()))
                .withAnnotations(resolveAnnotations(vc.getAnnotations()))
                ;
        resolvedNames = backup;
        return result;
    }


    private PList<RAnnotation> resolveAnnotations(PList<RAnnotation> annotations){
        return annotations.map(an -> {

            RClass name = resolveClass(an.getName());
            PMap<String,RConst> values = an.getValues();
            //TODO
            //Could be that an annotation value
            //Needs resolving, so we need to do that here


            //RAnnotationDef  atDef = findAnnotationDef(name);
            //values = values.map(t -> {
            //    atDef.getProperties().
            //});
            return an.withName(name).withValues(values);
        });
    }


    private RTypeSig resolveTypeSig(RTypeSig typeSig){
        return typeSig
                .withName(resolveClass(typeSig.getName()))
                .withGenerics(typeSig.getGenerics().map(this::resolveTypeSig));
    }
    private RInterfaceClass resolveInterfaceClass(RInterfaceClass ic){
        return ic
                .withName(resolveClass(ic.getName()))
                .withProperties(ic.getProperties().map(this::resolveProperty))
                .withAnnotations(resolveAnnotations(ic.getAnnotations()));

    }
    private RProperty resolveProperty(RProperty p){
        RValueType  vt = resolveValueType(p.getValueType());
        return p
                .withValueType(vt)
                .withAnnotations(resolveAnnotations(p.getAnnotations()))
                .withDefaultValue(p.getDefaultValue().map(dv -> resolveConst(vt.getTypeSig(),dv)).orElse(null))
                ;
    }

    private RConst resolveConst(RTypeSig expectedType,RConst cv){
        throw new ToDo(cv.toString());
    }

    private RValueType resolveValueType(RValueType vt){
        return vt.withTypeSig(resolveTypeSig(vt.getTypeSig()));
    }


    private RRemoteClass resolveRemoteClassDef(RRemoteClass rc){
        return rc
                .withName(resolveClass(rc.getName()))
                .withFunctions(rc.getFunctions().map(this::resolveFunctionDef))
                .withAnnotations(resolveAnnotations(rc.getAnnotations()));

    }
    private RFunction resolveFunctionDef(RFunction f){
        return f
                .withParams(f.getParams().map(this::resolveFunctionParam))
                .withResultType(f.getResultType().map(this::resolveValueType).orElse(null))
                .withAnnotations(resolveAnnotations(f.getAnnotations()))
                .withResultType(f.getResultType().map(this::resolveValueType).orElse(null));
    }

    private RFunctionParam resolveFunctionParam(RFunctionParam p){
        return p
                .withValueType(resolveValueType(p.getValueType()))
                .withAnnotations(resolveAnnotations(p.getAnnotations()));
    }


    /**
     * Resolves the class package by searching the original substema and
     * all imported substema's for the definition of the className.<br>
     * When found, the fully defined RClass is returned.<br>
     * If the class already has a package, return the supplied class.<br>
     *
     * @param cls The class to resolve.
     * @return The RClass with filled in packageName
     * @throws SubstemaException when there are multiple definitions found or when there is no defintion.
     */
    private RClass resolveClass(RClass cls){

        // Do we already have a package ?
        if(cls.getPackageName().isEmpty() == false){
            //Just return it
            return cls;
        }

        // Is this an internal substema class
        String clsName = cls.getClassName();
        if (SubstemaUtils.isSubstemaClass(cls)) {
            //Just return it
            return cls;
        }

        // Is this already resolved before ?
        RClass  res = resolvedNames.getOpt(clsName).orElse(null);
        if(res != null){
            //Return the previous resolved class
            return res;
        }


        // Is this defined in the substema we try to resolve ?
        res = findName(originalSubstema,clsName).orElse(null);
        if(res != null){
            //Set the packagename of the original substema
            res = res.withPackageName(packageName);
            resolvedNames = resolvedNames.put(clsName,res);
            return res;
        }

        // Build up a list with all definitions for this className
        // using the substema import list.
        PList<RClass> all = PList.empty();
        for(RSubstema s : originalSubstema.getImports().map(ri -> ri.getSubstema().orElse(null)).filterNulls()){
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


    /**
     * Find out if a substema contains the definition of a className by looking for a<br>
     * interface, value class, remote class, enum class or annotation definition in the substema.<br>
     * If the definition is found, then the resolveClass {@link RClass} with packageName is returned.<br>
     *
     * @param stema The substem to search in
     * @param className The classname to search for
     * @return the Optional RClass with packageName of the definition in the given substema
     * @throws SubstemaException Throws an exception if multiple definitions are found in the substema
     */
    private Optional<RClass> findName(RSubstema stema, String className){
        PList<RClass> res = PList.empty();

        // Find an interface class with this name
        res = res.plus(
                stema.getInterfaceClasses()
                        .find( ic -> ic.getName().getClassName().equals(className))
                        .map(i -> i.getName())
                        .orElse(null));

        // Find a value class with this name
        res = res.plus(
                stema.getValueClasses()
                        .find( c -> c.getTypeSig().getName().getClassName().equals(className))
                        .map(i -> i.getTypeSig().getName())
                        .orElse(null));

        // Find a remote class with this name
        res = res.plus(
                stema.getRemoteClasses()
                        .find( c -> c.getName().getClassName().equals(className))
                        .map(i -> i.getName())
                        .orElse(null));

        // Find an enum with this name
        res = res.plus(
                stema.getEnums()
                        .find(e -> e.getName().getClassName().equals(className))
                        .map(i -> i.getName())
                        .orElse(null));

        // Find an annotation definition with this name
        res = res.plus(
                stema.getAnnotationDefs()
                        .find(e -> e.getName().getClassName().equals(className))
                        .map(i -> i.getName())
                        .orElse(null));

        //Filter all the nulls
        res = res.filterNulls().plist();


        if(res.size() > 1){
            throw new SubstemaException("Multiple definitions found for " + className + ": " + res.toString(","));
        }

        return res.headOpt();
    }


}
