package com.persistentbit.robjects.testapi;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
public class UserSessionImpl implements UserSession{
    private final String userName;
    public UserSessionImpl(String userName){
        this.userName = userName;
    }
    @Override
    public UserData getDetails() {
        return new UserData(userName);
    }
}
