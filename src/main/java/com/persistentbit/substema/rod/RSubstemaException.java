package com.persistentbit.substema.rod;

/**
 * Created by petermuys on 17/09/16.
 */
public class RSubstemaException extends RuntimeException{
    public RSubstemaException() {
    }

    public RSubstemaException(String message) {
        super(message);
    }

    public RSubstemaException(String message, Throwable cause) {
        super(message, cause);
    }
}
