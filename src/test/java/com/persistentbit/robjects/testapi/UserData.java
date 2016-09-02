package com.persistentbit.robjects.testapi;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
public class UserData extends BaseValueClass {
    public final String userName;

    public UserData(String userName) {
        this.userName = userName;
    }
}
