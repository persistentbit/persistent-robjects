package com.persistentbit.substema.testapi;

import com.persistentbit.core.Tuple2;
import com.persistentbit.substema.annotations.Remotable;

import java.util.concurrent.CompletableFuture;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
@Remotable
public interface UserSession {
    CompletableFuture<UserData>    getDetails();

    CompletableFuture<UsersService>    getUsersService();

    CompletableFuture<Tuple2<Integer,String>>  createMeATuple(int v1, String v2);

    CompletableFuture<Object> testWithVoid();
    CompletableFuture<Object> showTuple(Tuple2<Integer,String> tupleParam);
}
