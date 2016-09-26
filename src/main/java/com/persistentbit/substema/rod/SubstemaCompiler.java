package com.persistentbit.substema.rod;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.dependencies.DependencyResolver;
import com.persistentbit.substema.dependencies.DependencySupplier;
import com.persistentbit.substema.rod.values.RImport;
import com.persistentbit.substema.rod.values.RSubstema;

/**
 * @author Peter Muys
 * @since 26/09/2016
 */
public class SubstemaCompiler {

    public PList<RSubstema>    compile(DependencySupplier dependencies, PList<String> packagesToCompile){
        Compiler c = new Compiler(dependencies);
        return packagesToCompile.map(p -> c.compile(p));
    }
    static private class Compiler{
        private final DependencySupplier dependencies;
        private PMap<String,RSubstema> parsed = PMap.empty();
        private PMap<String,RSubstema> compiled = PMap.empty();

        public Compiler(DependencySupplier dependencies) {
            this.dependencies = dependencies;
        }

        public RSubstema    parse(String packageName){
            RSubstema res = parsed.getOpt(packageName).orElse(null);
            if(res != null){
                return res;
            }
            String code = dependencies.apply(packageName).orElse(null);
            if(code == null){
                throw new RSubstemaException("Can't find code for package " + packageName );
            };
            RodParser parser = new RodParser(packageName,new RodTokenizer().tokenize(packageName,code));
            res =parser.parseSubstema();
            parsed = parsed.put(packageName,res);
            return res;
        }

        public RSubstema compile(String packageName){
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

            return res;
        }
    }
}
