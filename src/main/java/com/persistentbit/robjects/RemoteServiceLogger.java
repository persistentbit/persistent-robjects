package com.persistentbit.robjects;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Peter Muys
 * @since 22/09/2016
 */
public class RemoteServiceLogger implements RemoteService{
    private RemoteService   master;

    public RemoteServiceLogger(RemoteService master) {
        this.master = master;
    }

    @Override
    public CompletableFuture<RCallResult> call(RCall call) {
        System.out.println("Call:" + call);
        try{
            RCallResult result = master.call(call).get();
            System.out.println(" -->"+ result);
            return CompletableFuture.completedFuture(result);
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close(long timeOut, TimeUnit timeUnit) {

    }
}
