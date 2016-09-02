package com.persistentbit.robjects.testapi;

import com.persistentbit.core.collections.PList;

import java.util.Optional;

/**
 * @author Peter Muys
 * @since 2/09/2016
 */
public class UserServiceImpl implements UsersService{
    @Override
    public PList<UserData> getAllUsers() {
        PList<UserData> res = PList.empty();
        res = res.plusAll(PList.forString().plusAll("user1","testUser1","user2","user3").map(n -> new UserData(n)));
        return res;
    }

    @Override
    public Optional<Integer> testOptional(Integer value) {
        return Optional.ofNullable(value);
    }
}
