package com.persistentbit.substema.compiler;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.substema.compiler.values.*;
import com.persistentbit.substema.compiler.values.expr.RConst;
import com.persistentbit.substema.compiler.values.expr.RConstBoolean;
import com.persistentbit.substema.compiler.values.expr.RConstString;

import java.util.Optional;

/**
 * Utilities to access Annotations and Annotation Definitions.
 * @since 07/10/2016
 */
public class AnnotationsUtils {
    private final SubstemaCompiler  compiler;


    public AnnotationsUtils(SubstemaCompiler compiler) {
        this.compiler = compiler;
    }

    /**
     * Find the annotation definition for the given RClass in the main
     * substema or one of it's imports.
     * @param cls The Annotation Defintion class
     * @return The found annotation definition
     */
    public Optional<RAnnotationDef> getDef(RClass cls){
        RSubstema substema = compiler.compile(cls.getPackageName());
        return substema.getAnnotationDefs().find(a -> a.getName().equals(cls));
    }

    /**
     * Filter all the annotations with the given RClass annotation definition
     * from a given Annotations List
     * @param annotations The list to filter
     * @param cls The RClass to find
     * @return The found Annotations
     */
    public PList<RAnnotation> getAnnotation(PList<RAnnotation> annotations,RClass cls){
        return annotations.filter(a -> a.getName().equals(cls));
    }

    public Optional<RAnnotation> getOneAnnotation(PList<RAnnotation> annotations, RClass cls){
        PList<RAnnotation> l = getAnnotation(annotations,cls);
        return l.headOpt();
    }

    /**
     * Check if an annotation is defined in a list of annotations
     * @param annotations the list of annotations
     * @param cls the annotation to find
     * @return true if found
     */
    public boolean hasAnnotation(PList<RAnnotation> annotations,RClass cls){
        return annotations.lazy().find(a -> a.getName().equals(cls)).isPresent();
    }


    /**
     * Return a annotation property value as a String
     * @param annotation    The annotation
     * @param propertyName  The property to get
     * @return an optional String value
     */
    public Optional<String> getStringProperty(RAnnotation annotation, String propertyName){
        return getProperty(annotation,propertyName).map( rc -> {
                    if(rc instanceof RConstString == false){
                        throw new SubstemaException("Property '" + propertyName + "' must have a string value in annotation " + annotation);
                    }
                    return ((RConstString)rc).getValue();
                }
        );
    }
    /**
     * Return a annotation property value as a Boolean
     * @param annotation    The annotation
     * @param propertyName  The property to get
     * @return an optional Boolean value
     */
    public Optional<Boolean> getBooleanProperty(RAnnotation annotation, String propertyName){
        return getProperty(annotation,propertyName).map( rc -> {
                    if(rc instanceof RConstBoolean == false){
                        throw new SubstemaException("Property '" + propertyName + "' must have a boolean value in annotation " + annotation);
                    }
                    return ((RConstBoolean)rc).isValue();
                }
        );
    }

    /**
     * Return the RConst for the property with the given name in the given annotation.<br>
     * If the named property is not found we check if the property is a default
     * property and return the default value if it is.
     * @param annotation    The Annotation to search in.
     * @param propertyName  The property name te find
     * @return  The Optional value.
     */
    public Optional<RConst> getProperty(RAnnotation annotation, String propertyName){
        //First check and return the value for the actual property name if it exists
        Optional<RConst> result = annotation.getValues().getOpt(propertyName);
        if(result.isPresent()){
            return result;
        }
        //The Property name can be the default property name
        //So we check the definition if this is the case and
        //return the value for the property where the name is null.
        RAnnotationDef def = getDef(annotation.getName()).orElseThrow(() -> new SubstemaException("Can't find annotation def for " + annotation));

        //  Return the property default value if it is present.
        RProperty propWithName = def.getProperties().find(p -> p.getName().equals(propertyName)).orElse(null);
        if(propWithName != null && propWithName.getDefaultValue().isPresent()){
            return propWithName.getDefaultValue();
        }

        RProperty defProp = getDefaultProperty(def).orElseThrow(
                ()-> new SubstemaException("Can't find property with name '" + propertyName + "' in " + annotation)
        );
        if(defProp.getName().equals(propertyName)){
            return annotation.getValues().getOpt(null);
        }
        return Optional.empty();
    }

    /**
     * Find the name of the default property.<br>
     * If there is exactly 1 property without a default value, than this property  that is returned.<br>
     * Otherwise there is no 1 default and an empty response is returned.
     * @param def The definition to get the property from
     * @return An Optional RProperty
     */
    public Optional<RProperty> getDefaultProperty(RAnnotationDef def){
        PStream<RProperty> withoutDefaults = def.getProperties().lazy().filter(p -> p.getDefaultValue().isPresent()==false);
        if(withoutDefaults.isEmpty() || withoutDefaults.size()>1){
            return Optional.empty();
        }
        return withoutDefaults.headOpt();
    }
}
