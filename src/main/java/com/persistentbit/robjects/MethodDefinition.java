package com.persistentbit.robjects;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.properties.FieldNames;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;


public class MethodDefinition implements Serializable
{
    private final String methodName;
    private final Class<?> resultClass;
    private final String[] paramNames;
    private final Class<?>[] paramTypes;


    private static String[] getMethodParamNames(Method m) {
        FieldNames fn = m.getDeclaredAnnotation(FieldNames.class);
        if(fn != null){
            return fn.names();
        }
        Parameter[] params = m.getParameters();
        String[] result = new String[params.length];
        for(int t=0; t<params.length;t++){
            result[t] = params[t].getName();
        }
        return result;
    }

    public MethodDefinition(Method method){
        this(method.getName(),method.getReturnType(), method.getParameterTypes(),getMethodParamNames(method));

    }

    @Override
    public String toString() {
        String params = PStream.from(paramTypes).zip(PStream.from(paramNames)).map(t -> t._2.getSimpleName() + " " + t._1).toString(",");
        return "MethodDefinition[" + resultClass.getSimpleName() + "#" + methodName + "(" + params + ")]";
    }

    public MethodDefinition(String methodName, Class<?> resultClass, Class<?>[] paramTypes, String[] paramNames)
    {
        this.methodName = Objects.requireNonNull(methodName);
        this.resultClass = resultClass; //null means method with void
        this.paramTypes = Objects.requireNonNull(paramTypes);
        this.paramNames = Objects.requireNonNull(paramNames);

    }

    public String getMethodName()
    {
        return methodName;
    }


    public Class<?> getResultClass() {
        return resultClass;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodDefinition that = (MethodDefinition) o;

        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        //if (resultClass != null ? !resultClass.equals(that.resultClass) : that.resultClass != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(paramNames, that.paramNames)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(paramTypes, that.paramTypes);

    }

    @Override
    public int hashCode() {
        int result = methodName != null ? methodName.hashCode() : 0;
        //result = 31 * result + (resultClass != null ? resultClass.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(paramNames);
        result = 31 * result + Arrays.hashCode(paramTypes);
        return result;
    }
}
