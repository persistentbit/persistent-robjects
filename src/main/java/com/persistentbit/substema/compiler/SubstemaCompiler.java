package com.persistentbit.substema.compiler;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.dependencies.DependencyResolver;
import com.persistentbit.substema.compiler.values.RImport;
import com.persistentbit.substema.compiler.values.RSubstema;
import com.persistentbit.substema.dependencies.DependencySupplier;
import com.persistentbit.substema.dependencies.SupplierDef;
import com.persistentbit.substema.dependencies.SupplierType;

/**
 * @author Peter Muys
 * @since 26/09/2016
 */
public class SubstemaCompiler {

    private final DependencySupplier dependencies;
    private PMap<String,RSubstema> parsed = PMap.empty();
    private PMap<String,RSubstema> compiled = PMap.empty();
    private final PList<String> implicitImportPackages;

    public SubstemaCompiler(DependencySupplier dependencies, PList<String> implicitImportPackages) {
        this.dependencies = dependencies.withSuppliers(dependencies.getSuppliers().plus(new SupplierDef(SupplierType.resource,"/")));
        this.implicitImportPackages = implicitImportPackages;
    }
    public SubstemaCompiler(DependencySupplier dependencies) {
        this.dependencies = dependencies.withSuppliers(dependencies.getSuppliers().plus(new SupplierDef(SupplierType.resource,"/")));
        this.implicitImportPackages = PList.empty();
    }
    public SubstemaCompiler withImplicitImportPackages(PList<String> implicitImportPackages){
        return new SubstemaCompiler(dependencies,implicitImportPackages);
    }

    public PList<String>    getImplicitImportPackages(){
        return implicitImportPackages;
    }


    public RSubstema    parse(String packageName){
        RSubstema res = parsed.getOpt(packageName).orElse(null);
        if(res != null){
            return res;
        }
        String code = dependencies.apply(packageName).orElse(null);
        if(code == null){
            throw new SubstemaException("Can't find code for package " + packageName );
        }
        SubstemaParser parser = new SubstemaParser(packageName,new SubstemaTokenizer().tokenize(packageName,code));
        res = parser.parseSubstema();
        for(String implicit : implicitImportPackages){
            if(packageName.equals(implicit) == false && res.getImports().find(i -> i.getPackageName().equals(implicit)).isPresent() == false){
                res = res.withImports(res.getImports().plus(new RImport(packageName)));
            }
        }

        parsed = parsed.put(packageName,res);
        return res;
    }

    public RSubstema compile(String packageName){
        //System.out.println("Compiling " + packageName);
        RSubstema res = compiled.getOpt(packageName).orElse(null);
        if(res != null){
            return res;
        }
        res = parse(packageName);

        PList<RSubstema> dependencies = DependencyResolver.resolve(res,s ->s.getImports().map(i -> parse(i.getPackageName()))
        );
        res = res.withImports(
                res.getImports().map(i -> new RImport(i.getPackageName(),compile(i.getPackageName())))
        );
        res = ResolvePackageNames.resolve(this,res);
        compiled = compiled.put(packageName,res);
        return res;
    }
}
