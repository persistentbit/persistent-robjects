package com.persistentbit.robjects.rod;

/**
 * Created by petermuys on 17/09/16.
 */
public class RServiceException extends RuntimeException{
    public RServiceException() {
    }

    public RServiceException(String message) {
        super(message);
    }

    public RServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
