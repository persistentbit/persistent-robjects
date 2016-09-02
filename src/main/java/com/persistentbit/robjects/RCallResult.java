package com.persistentbit.robjects;

import com.persistentbit.core.Immutable;
import com.persistentbit.jjson.mapping.JJReader;
import com.persistentbit.jjson.mapping.impl.JJObjectReader;
import com.persistentbit.jjson.nodes.JJNode;
import com.persistentbit.jjson.nodes.JJNodeObject;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

@Immutable
public class RCallResult {
    private final MethodDefinition          theCall;
    private final Object                    value;
    private final RemoteObjectDefinition    robject;
    private final Exception                 exception;



    public RCallResult(MethodDefinition theCall,Object value, RemoteObjectDefinition robject, Exception exception) {
        this.theCall = theCall;
        this.value = value;
        this.robject = robject;
        this.exception = exception;
    }

    public Optional<Object> getValue() {
        return Optional.ofNullable(value);
    }

    public Optional<RemoteObjectDefinition> getRobject() {
        return Optional.ofNullable(robject);
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    static public RCallResult value(MethodDefinition theCall,Object v){
        return new RCallResult(theCall,v,null,null);
    }
    static public RCallResult robject(RemoteObjectDefinition r){
        return new RCallResult(null,null,r,null);
    }
    static public RCallResult exception(Exception r){
        return new RCallResult(null,null,null,r);
    }

    static public final JJObjectReader jsonReader = new JJObjectReader() {
        @Override
        public Object read(Type type, JJNode node, JJReader masterReader) {
            try{
                JJNodeObject obj = node.asObject().get();
                Object value = null;
                MethodDefinition md = masterReader.read(obj.get("theCall").get(),MethodDefinition.class);
                JJNode valueNode = obj.get("value").get();
                if(valueNode.asNull().isPresent() == false){

                    Method m = RemotableMethods.getRemotableMethod(md);
                    value = masterReader.read(valueNode, m.getReturnType(),m.getGenericReturnType());
                }
                RemoteObjectDefinition robject = masterReader.read(obj.get("robject").get(), RemoteObjectDefinition.class);
                Exception exception = masterReader.read(obj.get("exception").get(),Exception.class);
                return new RCallResult(md,value,robject,exception);

            } catch (Exception e){
                throw new RObjException(e);
            }
        }
    };
}
