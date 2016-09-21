package com.persistentbit.robjects.testapi;

import com.persistentbit.robjects.annotations.Remotable;
import com.persistentbit.robjects.annotations.RemoteCache;

import java.util.concurrent.CompletableFuture;

@Remotable
public interface App {
    @RemoteCache
    CompletableFuture<AppVersion>   getVersion();
    CompletableFuture<String>  createLoginToken(String un, String pw);
    CompletableFuture<UserSession> getUserSession(String loginToken);

}
