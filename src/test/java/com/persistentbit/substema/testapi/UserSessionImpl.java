package com.persistentbit.substema.testapi;

import com.persistentbit.core.Lazy;
import com.persistentbit.core.tuples.Tuple2;

import java.util.concurrent.CompletableFuture;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
public class UserSessionImpl implements UserSession{
    private final String userName;
    private final Lazy<UsersService> usersService;
    public UserSessionImpl(String userName){
        this.userName = userName;
        usersService = new Lazy<>(() -> new UserServiceImpl());
    }

    @Override
    public CompletableFuture<UsersService> getUsersService() {
        return CompletableFuture.completedFuture(usersService.get());
    }

    @Override
    public CompletableFuture<UserData> getDetails() {
        return CompletableFuture.completedFuture(new UserData(userName));
    }

    @Override
    public CompletableFuture<Tuple2<Integer, String>> createMeATuple(int v1, String v2) {
        return CompletableFuture.completedFuture(new Tuple2<>(v1,v2));
    }

    @Override
    public CompletableFuture<Object> testWithVoid() {
        System.out.println("testWithVoid called on UserSession !!!");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Object> showTuple(Tuple2<Integer, String> tupleParam) {
        System.out.println("show tuple:" + tupleParam);
        return CompletableFuture.completedFuture(null);
    }
}
