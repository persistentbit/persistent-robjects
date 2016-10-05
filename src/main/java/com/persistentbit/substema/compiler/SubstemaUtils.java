package com.persistentbit.substema.compiler;

import com.persistentbit.core.collections.PSet;
import com.persistentbit.substema.compiler.values.RClass;

/**
 * Created by petermuys on 1/10/16.
 */
public class SubstemaUtils {

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
    static public final RClass dateRClass = new RClass("","Date");
    static public final RClass dateTimeRClass = new RClass("","DateTime");

    static public final PSet<RClass> dateClasses = PSet.val(dateRClass,dateTimeRClass);


    static public final PSet<RClass> collectionClasses = PSet.val(listRClass,setRClass,mapRClass);

    static public boolean isDateClass(RClass cls){
        return dateClasses.contains(cls);
    }

    static public boolean isCollectionClass(RClass cls){
        return collectionClasses.contains(cls);
    }

    static public boolean isSubstemaClass(RClass cls){
        return isNumberClass(cls) || isCollectionClass(cls) || cls.equals(stringRClass)|| cls.equals(booleanRClass) || isDateClass(cls);
    }

    static public final PSet<RClass> numberClasses = PSet.val(
            byteRClass,shortRClass,integerRClass,longRClass,
            floatRClass,doubleRClass
    );
    static public boolean isNumberClass(RClass cls){
        return numberClasses.contains(cls);
    }
}
