package com.persistentbit.robjects;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.jjson.mapping.JJWriter;
import com.persistentbit.jjson.nodes.JJPrinter;
import com.persistentbit.jjson.security.JJSigning;

import java.util.Objects;

/**
 * @author Peter Muys
 * @since 2/09/2016
 */
public class RCallStack extends BaseValueClass {
    private final PList<RMethodCall> callStack;
    private final String signature;

    public RCallStack(String signature){
        this(signature,PList.empty());
    }
    public RCallStack(String signature,PList<RMethodCall> callStack) {

        this.callStack = Objects.requireNonNull(callStack);
        this.signature = Objects.requireNonNull(signature);

    }

    public PList<RMethodCall> getCallStack() {
        return callStack;
    }


    public String getSignature() {
        return signature;
    }

    static public RCallStack    createAndSign(PList<RMethodCall> methods,JJWriter jsonWriter,String secret){
        String msg = JJPrinter.print(false,jsonWriter.write(methods))+secret;
        return new RCallStack(JJSigning.sign(msg,"SHA-256"),methods);
    }
    public boolean verifySignature(String secret, JJWriter jsonWriter){
        String msg = JJPrinter.print(false,jsonWriter.write(callStack))+secret;
        return JJSigning.sign(msg,"SHA-256").equals(signature);
    }


}
