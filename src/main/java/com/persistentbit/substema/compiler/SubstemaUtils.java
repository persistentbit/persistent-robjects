package com.persistentbit.substema.compiler;

import com.persistentbit.core.collections.PSet;
import com.persistentbit.substema.compiler.values.RClass;

/**
 * General Substema utilities.<br>
 * @author Peter Muys
 * @since 1/10/16
 */
public final class SubstemaUtils{

    public static final String annotationsPackage = "com.persistentbit.substema.annotations";

    public static final RClass docRClass  = new RClass(annotationsPackage, "Doc");
    public static final RClass noToString = new RClass(annotationsPackage, "NoToString");

    public static final RClass stringRClass   = new RClass("", "String");
    public static final RClass booleanRClass  = new RClass("", "Boolean");
    public static final RClass listRClass     = new RClass("", "List");
    public static final RClass setRClass      = new RClass("", "Set");
    public static final RClass mapRClass      = new RClass("", "Map");
    public static final RClass integerRClass  = new RClass("", "Integer");
    public static final RClass byteRClass     = new RClass("", "Byte");
    public static final RClass shortRClass    = new RClass("", "Short");
    public static final RClass longRClass     = new RClass("", "Long");
    public static final RClass floatRClass    = new RClass("", "Float");
    public static final RClass doubleRClass   = new RClass("", "Double");
    public static final RClass dateRClass     = new RClass("", "Date");
    public static final RClass dateTimeRClass = new RClass("", "DateTime");

    public static final PSet<RClass> dateClasses = PSet.val(dateRClass, dateTimeRClass);


    public static final PSet<RClass> collectionClasses = PSet.val(listRClass, setRClass, mapRClass);
    public static final PSet<RClass> numberClasses     = PSet.val(
        byteRClass, shortRClass, integerRClass, longRClass,
        floatRClass, doubleRClass
    );

    public static boolean isSubstemaClass(RClass cls) {
        return isNumberClass(cls) || isCollectionClass(cls) || cls.equals(stringRClass) || cls
            .equals(booleanRClass) || isDateClass(cls);
    }

    public static boolean isNumberClass(RClass cls) {
        return numberClasses.contains(cls);
    }

    public static boolean isCollectionClass(RClass cls) {
        return collectionClasses.contains(cls);
    }

    public static boolean isDateClass(RClass cls) {
        return dateClasses.contains(cls);
    }
}
