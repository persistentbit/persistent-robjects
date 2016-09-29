package com.persistentbit.substema.compiler;

/**
 * Created by petermuys on 17/09/16.
 */
public class SubstemaException extends RuntimeException{
    public SubstemaException() {
    }

    public SubstemaException(String message) {
        super(message);
    }

    public SubstemaException(String message, Throwable cause) {
        super(message, cause);
    }
}
