package com.persistentbit.robjects;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.jjson.mapping.JJWriter;
import com.persistentbit.jjson.nodes.JJPrinter;
import com.persistentbit.jjson.security.JJSigning;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by petermuys on 18/09/16.
 */
public class RSessionData extends BaseValueClass{
    public String data;
    public LocalDateTime   validUntil;
    public String signature;


    public RSessionData(String data, LocalDateTime validUntil, String signature) {
        this.data = data;
        this.validUntil = validUntil;
        this.signature = signature;
    }
    public RSessionData(String data, LocalDateTime validUntil) {
        this(data,validUntil,null);
    }

    public RSessionData    signed(String secret){
        String sig = JJSigning.sign(this.data + this.validUntil.format(DateTimeFormatter.ISO_DATE_TIME) + secret,"SHA-256");
        return new RSessionData(data,validUntil,sig);
    }
    public boolean verifySignature(String secret){
        RSessionData signed = this.signed(secret);
        return signed.signature.equals(this.signature);
    }



}
