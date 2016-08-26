package com.persistentbit.robjects;
import com.persistentbit.core.collections.PList;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Objects;


public class MethodDefinition implements Serializable
{
    private String methodName;
    private String resultClassName;
    private PList<ParamDefinition> parameters;


    public MethodDefinition(Method method){
        this(method.getName(),method.getReturnType().getName(),ParamDefinition.forMethod(method));
    }

    public MethodDefinition(String methodName, String resultClassName, PList<ParamDefinition> parameters)
    {
        this.methodName = Objects.requireNonNull(methodName);
        this.resultClassName = Objects.requireNonNull(resultClassName);
        this.parameters = Objects.requireNonNull(parameters);
    }

    public String getMethodName()
    {
        return methodName;
    }

    public String getResultClassName()
    {
        return resultClassName;
    }

    public PList<ParamDefinition> getParameters()
    {
        return parameters;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MethodDefinition that = (MethodDefinition) o;

        if (!methodName.equals(that.methodName))
            return false;
        if (!resultClassName.equals(that.resultClassName))
            return false;

        return parameters.equals(that.getParameters());

    }

    @Override
    public int hashCode()
    {
        int result = methodName.hashCode();
        result = 31 * result + resultClassName.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "MethodDef[" + methodName+ "(" + parameters+ ")]";
    }
}
