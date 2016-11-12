package com.persistentbit.substema;

import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.jjson.security.JJSigning;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a Session Data String with an expiration date that is signed.<br>
 * Is used to store session data in a Remote Call result {@link RCallResult}. <br>
 *
 * @author Peter Muys
 * @since 18/09/16
 * @see RCallResult
 * @see RServer#call(RCall)
 */
public class RSessionData extends BaseValueClass{
    public String data;
    public LocalDateTime   validUntil;
    public String signature;

    /**
     * @param data       The Data string representing the session data.
     * @param validUntil The time this session data is valid
     * @param signature  The signature of this structure, normally signed by the service implementation secret
     */
    public RSessionData(String data, LocalDateTime validUntil, String signature) {
        this.data = data;
        this.validUntil = validUntil;
        this.signature = signature;
    }
    public RSessionData(String data, LocalDateTime validUntil) {
        this(data,validUntil,null);
    }

    /**
     * Create a signed version of this session data
     * @param secret The secret, normally comming from the {@link RServer}
     * @return The signed version of this instance.
     */
    public RSessionData    signed(String secret){
        String sig = JJSigning.sign(this.data + this.validUntil.format(DateTimeFormatter.ISO_DATE_TIME) + secret,"SHA-256");
        return new RSessionData(data,validUntil,sig);
    }

    /**
     * Check if the signature is correct for the given secret.
     *
     * @param secret The secret, normally comming from the {@link RServer}
     *
     * @return true if the signature is correct
     */
    public boolean verifySignature(String secret){
        RSessionData signed = this.signed(secret);
        return signed.signature.equals(this.signature);
    }



}
