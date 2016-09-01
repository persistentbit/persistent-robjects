package com.persistentbit.robjects;

import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.security.JJSigning;
import com.persistentbit.robjects.testapi.*;
import org.junit.Test;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
public class TestRObjects {

    @Test
    public void test() {
        JJMapper mapper = new JJMapper();

        AppImpl appImpl = new AppImpl(mapper,new JJSigning("testAppSecret"));
        RemoteService rserver = new RServer<>(App.class,() -> appImpl);
        rserver = new JSonRemoteService(rserver);
        App app = RProxy.create(rserver);
        AppVersion version = app.getVersion();
        System.out.println(app.getVersion());
        assert version.name.equals("RName");
        assert version.environment == AppVersion.RunEnvironment.develop;
        String lt = app.createLoginToken("notok","notok");
        assert lt == null;
        final String un = "testPeterMuys";
        lt = app.createLoginToken(un,"testPassword");
        assert lt != null;
        UserSession us = app.getUserSession("wrongToken");
        assert us == null;
        us = app.getUserSession(lt);
        assert us != null;
        UserData ud = us.getDetails();
        assert ud.userName.equals(un);
        us.testWithVoid();
        System.out.println("Got Tuple: " + us.createMeATuple(1,"test"));
    }


    static public void main(String...args){
        new TestRObjects().test();
    }
}
