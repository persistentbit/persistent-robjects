package com.persistentbit.substema.javagen;

import com.persistentbit.substema.compiler.values.RClass;
import com.persistentbit.substema.compiler.values.RSubstema;
import com.persistentbit.substema.compiler.values.RTypeSig;

import java.util.Optional;

/**
 * Created by petermuys on 30/09/16.
 */
public class JavaGenUtils {



    static public RClass toRClass(Class<?> cls){
        return new RClass(cls.getPackage().getName(),cls.getSimpleName());
    }

    static public String toString(String defaultPackage,Class<?> cls){
        return toString(defaultPackage,toRClass(cls));
    }

    static public String toString(String defaultPackage,RClass cls){
        if(cls.getPackageName().equals(defaultPackage) || cls.getPackageName().isEmpty()){
            return cls.getClassName();
        }
        return cls.getPackageName() + "." + cls.getClassName();
    }
    static public String toString(String defaultPackage,RTypeSig typeSig){
        RClass cls = typeSig.getName();
        String res;
        if(cls.getPackageName().equals(defaultPackage) || cls.getPackageName().isEmpty()){
            res = cls.getClassName();
        } else {
            res = cls.getPackageName() + "." + cls.getClassName();
        }
        String gen = typeSig.getGenerics().map(g -> toString(defaultPackage,g)).toString(",");
        res += gen.isEmpty() ? "" : "<" + gen + ">";
        return res;
    }

    static public Optional<String> genericsToString(String defaultPackage, RTypeSig typeSig){
        String res = typeSig.getGenerics().map(g -> toString(defaultPackage,g)).toString(", ");
        return res.isEmpty() ? Optional.empty() : Optional.of(res);
    }
}
