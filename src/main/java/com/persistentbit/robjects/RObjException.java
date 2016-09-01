package com.persistentbit.robjects;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
public class RObjException extends RuntimeException{
    public RObjException() {
        super();
    }

    public RObjException(String message) {
        super(message);
    }

    public RObjException(String message, Throwable cause) {
        super(message, cause);
    }

    public RObjException(Throwable cause) {
        super(cause);
    }

    protected RObjException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
