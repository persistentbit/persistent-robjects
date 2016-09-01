package com.persistentbit.robjects.testapi;

import com.persistentbit.robjects.annotations.Remotable;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
@Remotable
public interface UserSession {
    UserData    getDetails();

}
