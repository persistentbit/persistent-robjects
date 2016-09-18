package com.persistentbit.robjects.runtestimpl;

import com.persistentbit.generated.runtest.User;
import com.persistentbit.generated.runtest.UserSession;
import com.persistentbit.robjects.RSessionManager;

import java.util.concurrent.CompletableFuture;

/**
 * Created by petermuys on 18/09/16.
 */
public class UserSessionImpl implements UserSession{
    private final User user;
    private final RSessionManager<RodTestSessionData>   sessionManager;

    public UserSessionImpl(User user, RSessionManager<RodTestSessionData> sessionManager) {
        this.user = user;
        this.sessionManager = sessionManager;
    }

    @Override
    public CompletableFuture<User> getUser() {
        return CompletableFuture.completedFuture(user);
    }
}
