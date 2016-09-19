package com.persistentbit.robjects.runtestimpl;

/**
 * Created by petermuys on 18/09/16.
 */
public class RodTestImpl {}/*implements RodTest{
    private final RSessionManager<RodTestSessionData>   sessionManager;
    static private final User peter = new User(1234,"mup",new Name("Peter","Muys"));
    public RodTestImpl(RSessionManager<RodTestSessionData> sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public CompletableFuture<User> login(String userName, String passWord) {
        System.out.println("login(" + userName + "," + passWord + ")");
        if(userName.equals("mup") && passWord.equals("pw")){
            sessionManager.setData(new RodTestSessionData(peter.id,peter.name), LocalDateTime.now().plusMinutes(15));
            return CompletableFuture.completedFuture(peter);
        }
        return null;
    }

    @Override
    public CompletableFuture<Optional<UserSession>> getUserSession() {
        RodTestSessionData sd = sessionManager.getData().orElse(null);
        if(sd != null){
            if(sd.userId == peter.id){
                return CompletableFuture.completedFuture(Optional.of(new UserSessionImpl(peter,sessionManager)));
            }
        }
        return null;
    }

    static private RodInfo rodInfo = new RodInfo("ROD Test App","1.1",peter.name);
    @Override
    public CompletableFuture<RodInfo> getInfo() {
        return CompletableFuture.completedFuture(rodInfo);
    }
}*/
