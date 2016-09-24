package com.persistentbit.substema.testapi;

import com.persistentbit.substema.annotations.Remotable;
import com.persistentbit.substema.annotations.RemoteCache;

import java.util.concurrent.CompletableFuture;

@Remotable
public interface App {
    @RemoteCache
    CompletableFuture<AppVersion>   getVersion();
    CompletableFuture<String>  createLoginToken(String un, String pw);
    CompletableFuture<UserSession> getUserSession(String loginToken);

}
