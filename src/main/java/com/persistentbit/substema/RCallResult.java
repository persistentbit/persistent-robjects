package com.persistentbit.substema;

import com.persistentbit.core.Immutable;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Result;
import com.persistentbit.jjson.mapping.impl.JJObjectReader;
import com.persistentbit.jjson.nodes.JJNode;
import com.persistentbit.jjson.nodes.JJNodeObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

@Immutable
public class RCallResult {

    private final MethodDefinition               theCall;
    private final RSessionData                   sessionData;
    private final Result                         result;
    private final Result<RemoteObjectDefinition> rod;


    public RCallResult(
        MethodDefinition theCall,
        RSessionData sessionData,
        Result result,
        Result<RemoteObjectDefinition> rod
    ) {
        this.theCall = theCall;
        this.sessionData = sessionData;
        this.result = result;
        this.rod = rod;
    }


    static public RCallResult forRootRemoteObject(RSessionData sessionData, Result<RemoteObjectDefinition> rod) {
        return new RCallResult(null, sessionData, null, rod);
    }

    static public RCallResult forRemoteObject(MethodDefinition call, RSessionData sessionData,
                                              Result<RemoteObjectDefinition> rod
    ) {
        return new RCallResult(call, sessionData, null, rod);
    }

    static public RCallResult forResultValue(MethodDefinition call, RSessionData sessionData, Result value) {
        return new RCallResult(call, sessionData, value, null);
    }


    @Override
    public String toString() {
        return "RCallResult{" +
            "theCall=" + theCall +
            ", sessionData=" + sessionData +
            ", result=" + result +
            ", rod=" + rod +
            '}';
    }

    public Optional<Result<Object>> getResult() {
        return Optional.ofNullable(result);
    }

    public Optional<MethodDefinition> getTheCall() {
        return Optional.ofNullable(theCall);
    }

    public Optional<Result<RemoteObjectDefinition>> getRod() {
        return Optional.ofNullable(rod);
    }

    public Optional<RSessionData> getSessionData() {
        return Optional.ofNullable(sessionData);
    }

    public static final JJObjectReader jsonReader = (type, node, masterReader) ->
        Log.function().code(l -> {
                                JJNodeObject     obj         = node.asObject().orElseThrow();
                                MethodDefinition md          = masterReader.read(obj.get("theCall").get(), MethodDefinition.class);
                                RSessionData     sessionData = masterReader.read(obj.get("sessionData").get(), RSessionData.class);
                                JJNode           valueNode   = obj.get("result").get();
                                Result           value       = null;
                                if(valueNode.asNull().isPresent() == false) {
                                    Method   m             = RemotableMethods.getRemotableMethod(md);
                                    Class<?> returnType    = m.getReturnType();
                                    Type     genReturnType = m.getGenericReturnType();
                                    value = (Result) masterReader.read(valueNode, returnType, genReturnType);
                                }
                                Result<RemoteObjectDefinition> rod     = null;
                                JJNode                         rodNode = obj.get("rod").get();
                                if(rodNode.asNull().isPresent() == false) {
                                    Field    f             = RCallResult.class.getDeclaredField("rod");
                                    Class<?> clsRodResult  = f.getType();
                                    Type     typeRodResult = f.getGenericType();
                                    rod = (Result<RemoteObjectDefinition>) masterReader.read(rodNode, clsRodResult, typeRodResult);
                                }
                                return new RCallResult(md, sessionData, value, rod);
                            }
        );


    /*static public final JJObjectReader jsonReader = (type, node, masterReader) -> {
        try{
			JJNodeObject obj = node.asObject().orElseThrow();
			Result value = null;
			MethodDefinition md = masterReader.read(obj.get("theCall").get(),MethodDefinition.class);
			JJNode valueNode = obj.get("value").get();
			if(valueNode.asNull().isPresent() == false){

				Method m = RemotableMethods.getRemotableMethod(md);
				Class<?> returnType = m.getReturnType();
				Type genReturnType = m.getGenericReturnType();
				if(returnType == Result.class){
					ParameterizedType pt = (ParameterizedType)genReturnType;
					genReturnType = pt.getActualTypeArguments()[0];
					returnType = ReflectionUtils.classFromType(genReturnType);
				}
				value = (Result)masterReader.read(valueNode, returnType,genReturnType);
			}
			RemoteObjectDefinition robject = masterReader.read(obj.get("robject").get(), RemoteObjectDefinition.class);

			RSessionData sessionData = masterReader.read(obj.get("sessionData").orElse(JJNodeNull.Null),RSessionData.class);
			return new RCallResult(md,value,robject,exception,sessionData);

		} catch (Exception e){
			throw new RObjException(e);
		}
	};*/
}
