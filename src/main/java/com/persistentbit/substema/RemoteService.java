package com.persistentbit.substema;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Peter Muys
 */
public interface RemoteService {
    default CompletableFuture<RCallResult>  getRoot(){
        return call(new RCall(null,null));
    }
    CompletableFuture<RCallResult>  call(RCall call);

    default void close(){
        close(Integer.MAX_VALUE,TimeUnit.DAYS);
    }
    void close(long timeOut, TimeUnit timeUnit);
}
