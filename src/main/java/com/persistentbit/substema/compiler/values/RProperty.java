package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.substema.compiler.values.expr.RConst;

import java.util.Optional;

/**
 * Contains the defintion of a named Substema property.<br>
 * The property can be an interface, case class or annotation property.<br>
 * Every property can also have an optional default constant value ({@link RConst})
 * @see RInterfaceClass
 * @see RValueClass
 * @see RAnnotationDef
 * @see RConst
 * @since 14/09/16
 * @author Peter Muys
 */
public class RProperty extends BaseValueClass {
    private final String     name;
    private final RValueType valueType;
    private final RConst defaultValue;
    private final PList<RAnnotation> annotations;

    public RProperty(String name, RValueType valueType, RConst defaultValue, PList<RAnnotation> annotations) {
        this.name = name;
        this.valueType = valueType;
        this.defaultValue = defaultValue;
        this.annotations = annotations;
    }
    public RProperty(String name, RValueType valueType,PList<RAnnotation> annotations){
        this(name,valueType,null,annotations);
    }

    public String getName() {
        return name;
    }

    public RValueType getValueType() {
        return valueType;
    }

    public PList<RAnnotation> getAnnotations() {
        return annotations;
    }

    public Optional<RConst> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public RProperty withName(String name){
        return copyWith("name",name);
    }
    public RProperty withValueType(RValueType vt){
        return copyWith("valueType",vt);
    }
    public RProperty withDefaultValue(RConst value){
        return copyWith("defaultValue",value);
    }
    public RProperty withAnnotations(PList<RAnnotation> annotations) { return copyWith("annotations",annotations);}
}