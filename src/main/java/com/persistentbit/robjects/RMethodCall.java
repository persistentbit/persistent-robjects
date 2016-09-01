package com.persistentbit.robjects;

import com.persistentbit.core.collections.PList;
import com.persistentbit.jjson.mapping.JJReader;
import com.persistentbit.jjson.mapping.impl.JJObjectReader;
import com.persistentbit.jjson.nodes.JJNode;
import com.persistentbit.jjson.nodes.JJNodeObject;

import java.lang.reflect.Type;
import java.util.Arrays;

public class RMethodCall {
    private final MethodDefinition  methodToCall;
    private final Object[]          arguments;



    public RMethodCall(MethodDefinition methodToCall, Object[] arguments){
        this.methodToCall = methodToCall;
        this.arguments = arguments;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public MethodDefinition getMethodToCall() {
        return methodToCall;
    }





    @Override
    public String toString() {
        return "RMethodCall{" +
                "methodToCall=" + methodToCall +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
    }

    static public final JJObjectReader jsonReader = new JJObjectReader() {
        @Override
        public Object read(Type type, JJNode node, JJReader masterReader) {
            if(node.getType() == JJNode.JType.jsonNull){
                return null;
            }
            JJNodeObject obj = node.asObject().get();
            MethodDefinition md = masterReader.read(obj.get("methodToCall").get(),MethodDefinition.class);
            JJNode argNode = obj.get("arguments").get();
            if(argNode.asNull().isPresent()){
                return new RMethodCall(md,new Object[0]);
            }
            PList<JJNode> items = argNode.asArray().get().pstream().plist();
            Object[] res = new Object[md.getParamTypes().length];
            for(int t=0; t<md.getParamTypes().length; t++){
                JJNode n = items.get(t);
                res[t] = masterReader.read(n,md.getParamTypes()[t]);
            }
            return new RMethodCall(md,res);
        }
    };
}
