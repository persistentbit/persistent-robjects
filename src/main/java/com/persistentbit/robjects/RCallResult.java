package com.persistentbit.robjects;

import com.persistentbit.core.Immutable;
import com.persistentbit.jjson.mapping.JJReader;
import com.persistentbit.jjson.mapping.impl.JJObjectReader;
import com.persistentbit.jjson.nodes.JJNode;
import com.persistentbit.jjson.nodes.JJNodeObject;

import java.lang.reflect.Type;
import java.util.Optional;

@Immutable
public class RCallResult {
    private final String resultClassName;
    private final Object                    value;
    private final RemoteObjectDefinition    robject;
    private final Exception exception;



    public RCallResult(Object value, RemoteObjectDefinition robject, Exception exception) {
        this.resultClassName = value == null ? null : value.getClass().getName();
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

    static public RCallResult value(Object v){
        return new RCallResult(v,null,null);
    }
    static public RCallResult robject(RemoteObjectDefinition r){
        return new RCallResult(null,r,null);
    }
    static public RCallResult exception(Exception r){
        return new RCallResult(null,null,r);
    }

    static public final JJObjectReader jsonReader = new JJObjectReader() {
        @Override
        public Object read(Type type, JJNode node, JJReader masterReader) {
            try{
                JJNodeObject obj = node.asObject().get();
                Object value = null;
                JJNode valueNode = obj.get("value").get();
                if(valueNode.asNull().isPresent() == false){
                    value = masterReader.read(valueNode,Class.forName(obj.get("resultClassName").get().asString().get().toString()));
                }
                RemoteObjectDefinition robject = masterReader.read(obj.get("robject").get(), RemoteObjectDefinition.class);
                Exception exception = masterReader.read(obj.get("exception").get(),Exception.class);
                return new RCallResult(value,robject,exception);

            } catch (Exception e){
                throw new RObjException(e);
            }
        }
    };
}
