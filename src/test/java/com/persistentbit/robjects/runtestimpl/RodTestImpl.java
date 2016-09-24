package com.persistentbit.robjects.runtestimpl;


import com.persistentbit.robjects.RSessionManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by petermuys on 18/09/16.
 */
public class RodTestImpl{}/* implements RodTest {
    private final RSessionManager<RodTestSessionData> sessionManager;
    static private final User peter = new User(1234,"mup",new Name("Peter","Muys"));
    public RodTestImpl(RSessionManager<RodTestSessionData> sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public CompletableFuture<User> login(String userName, String passWord) {
        System.out.println("login(" + userName + "," + passWord + ")");
        if(userName.equals("mup") && passWord.equals("pw")){
            sessionManager.setData(new RodTestSessionData(peter.getId(),peter.getName()), LocalDateTime.now().plusMinutes(15));
            return CompletableFuture.completedFuture(peter);
        }
        return null;
    }

    @Override
    public CompletableFuture<Optional<UserSession>> getUserSession() {
        RodTestSessionData sd = sessionManager.getData().orElse(null);
        if(sd != null){
            if(sd.userId == peter.getId()){
                return CompletableFuture.completedFuture(Optional.of(new UserSessionImpl(peter,sessionManager)));
            }
        }
        return null;
    }

    static private RodInfo rodInfo = new RodInfo("ROD Test App","1.1",peter.getName());
    @Override
    public CompletableFuture<RodInfo> getInfo() {
        return CompletableFuture.completedFuture(rodInfo);
    }
}*/
