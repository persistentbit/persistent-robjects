package com.persistentbit.robjects;


import com.persistentbit.core.collections.PList;
import com.persistentbit.core.properties.FieldNames;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 */
public class ParamDefinition
{
    private final String name;
    private final String typeName;



    public ParamDefinition(String typeName, String name)
    {
        this.name = name;
        this.typeName = typeName;
    }
    public ParamDefinition(String typeName){
        this.typeName = typeName;
        this.name = "";
    }

    static public PList<ParamDefinition> forMethod(Method method){
        FieldNames fn = method.getDeclaredAnnotation(FieldNames.class);
        PList<ParamDefinition> result = PList.empty();

        for(int t=0; t<method.getParameterCount();t++){
            Parameter p = method.getParameters()[t];
            Class<?> cls = method.getParameterTypes()[t] ;
            String name = p.getName();
            if(fn != null){
                name = fn.names()[t];
            }
            result = result.plus(new ParamDefinition(cls.getName(), name));
        }
        return result;
    }

    public String getName()
    {
        return name;
    }

    public String getTypeName()
    {
        return typeName;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ParamDefinition that = (ParamDefinition) o;


        return typeName.equals(that.typeName) && name.equals(that.name);

    }

    @Override
    public int hashCode()
    {
        int result = typeName.hashCode() + name.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "Param[" + getName() + ":" + getTypeName() + "]";
    }
}
