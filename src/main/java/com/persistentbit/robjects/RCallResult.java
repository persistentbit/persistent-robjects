package com.persistentbit.robjects;

import com.persistentbit.core.Immutable;
import com.persistentbit.core.utils.ReflectionUtils;
import com.persistentbit.jjson.mapping.JJReader;
import com.persistentbit.jjson.mapping.impl.JJObjectReader;
import com.persistentbit.jjson.nodes.JJNode;
import com.persistentbit.jjson.nodes.JJNodeNull;
import com.persistentbit.jjson.nodes.JJNodeObject;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Immutable
public class RCallResult {
    static private Logger log = Logger.getLogger(RCallResult.class.getName());
    private final MethodDefinition          theCall;
    private final Object                    value;
    private final RemoteObjectDefinition    robject;
    private final Exception                 exception;
    private final RSessionData              sessionData;


    public RCallResult(MethodDefinition theCall,Object value, RemoteObjectDefinition robject, Exception exception,RSessionData sessionData) {
        this.theCall = theCall;
        this.value = value;
        this.robject = robject;
        this.exception = exception;
        this.sessionData = sessionData;
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

    public Optional<RSessionData> getSessionData() {
        return Optional.ofNullable(sessionData);
    }

    static public RCallResult value(RSessionData sessionData,MethodDefinition theCall, Object v){
        return new RCallResult(theCall,v,null,null,sessionData);
    }
    static public RCallResult robject(RSessionData sessionData,RemoteObjectDefinition r){
        return new RCallResult(null,null,r,null,sessionData);
    }
    static public RCallResult exception(RSessionData sessionData,Exception r){
        return new RCallResult(null,null,null,r,sessionData);
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
                    Class<?> returnType = m.getReturnType();
                    Type genReturnType = m.getGenericReturnType();
                    if(returnType == CompletableFuture.class){
                        ParameterizedType pt = (ParameterizedType)genReturnType;
                        genReturnType = pt.getActualTypeArguments()[0];
                        returnType = ReflectionUtils.classFromType(genReturnType);
                    }
                    value = masterReader.read(valueNode, returnType,genReturnType);
                }
                RemoteObjectDefinition robject = masterReader.read(obj.get("robject").get(), RemoteObjectDefinition.class);
                Exception exception = masterReader.read(obj.get("exception").get(),Exception.class);
                RSessionData sessionData = masterReader.read(obj.get("sessionData").orElse(JJNodeNull.Null),RSessionData.class);
                return new RCallResult(md,value,robject,exception,sessionData);

            } catch (Exception e){
                log.severe(e.getMessage());
                e.printStackTrace();
                throw new RObjException(e);
            }
        }
    };
}
