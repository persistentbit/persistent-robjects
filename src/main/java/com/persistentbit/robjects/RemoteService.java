package com.persistentbit.robjects;

/**
 * @author Peter Muys
 */
public interface RemoteService {
    RCallResult  getRoot();
    RCallResult  call(RCall call);
}
