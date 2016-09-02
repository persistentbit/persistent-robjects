package com.persistentbit.robjects.testapi;

import com.persistentbit.core.Tuple2;

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

    @Override
    public Tuple2<Integer, String> createMeATuple(int v1, String v2) {
        return new Tuple2<>(v1,v2);
    }

    @Override
    public void testWithVoid() {
        System.out.println("testWithVoid called on UserSession !!!");
    }

    @Override
    public void showTuple(Tuple2<Integer, String> tupleParam) {
        System.out.println("show tuple:" + tupleParam);
    }
}
