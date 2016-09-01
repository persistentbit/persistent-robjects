package com.persistentbit.robjects;

/**
 * @author Peter Muys
 */
public interface RemoteService {
    default RCallResult  getRoot(){
        return call(new RCall());
    }
    RCallResult  call(RCall call);
}
