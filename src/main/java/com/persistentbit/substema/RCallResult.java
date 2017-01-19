package com.persistentbit.substema;

import com.persistentbit.core.Immutable;
import com.persistentbit.core.result.Result;

import java.util.Optional;

@Immutable
public class RCallResult {

    private final MethodDefinition       theCall;
    private final RSessionData           sessionData;
    private final Result                 result;
    private final RemoteObjectDefinition rod;


    public RCallResult(MethodDefinition theCall, RSessionData sessionData, Result result, RemoteObjectDefinition rod) {
        this.theCall = theCall;
        this.sessionData = sessionData;
        this.result = result;
        this.rod = rod;
    }

    public RCallResult(MethodDefinition call, RSessionData sessionData, Result result) {
        this(call, sessionData, result, null);
    }

    public RCallResult(MethodDefinition call, RSessionData sessionData, RemoteObjectDefinition rod) {
        this(call, sessionData, null, rod);
    }

    public RCallResult(RSessionData sessionData, RemoteObjectDefinition rod) {
        this(null, sessionData, rod);
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

    public Optional<RemoteObjectDefinition> getRod() {
        return Optional.ofNullable(rod);
    }

    public Optional<RSessionData> getSessionData() {
        return Optional.ofNullable(sessionData);
    }



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
