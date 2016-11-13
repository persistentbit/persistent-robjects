package com.persistentbit.substema;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A RemoteService provides a standard way to call remote or local services.<br>
 * All calls are executed async and return a {@link CompletableFuture}.<br>
 * The implementation (remote, local, http, tcp,...) is completely hidden for the client.<br>
 * @author Peter Muys
 * @see RServer
 * @see HttpRemoteServiceClient
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
