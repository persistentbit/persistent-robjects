package com.persistentbit.robjects.testapi;

import com.persistentbit.robjects.annotations.Remotable;
import com.persistentbit.robjects.annotations.RemoteCache;

@Remotable
public interface App {
    @RemoteCache
    AppVersion   getVersion();
    String  createLoginToken(String un, String pw);
    UserSession getUserSession(String loginToken);

}
