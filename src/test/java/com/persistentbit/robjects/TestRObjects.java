package com.persistentbit.robjects;

import com.persistentbit.core.Tuple2;
import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.security.JJSigning;
import com.persistentbit.robjects.testapi.*;
import org.junit.Test;

import java.util.Optional;
import java.util.logging.*;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
public class TestRObjects {

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s [%3$s] (%2$s) %5$s %6$s%n");
        Logger global = Logger.getLogger("");
        global.setLevel(Level.FINE);
        ConsoleHandler ch= new ConsoleHandler();
        ch.setLevel(Level.FINEST);
        global.addHandler(ch);
    }

    @Test
    public void test() {
        JJMapper mapper = new JJMapper();

        AppImpl appImpl = new AppImpl(mapper,new JJSigning("testAppSecret"));
        RemoteService rserver = new RServer<>("Dit is het geheim",App.class,() -> appImpl);
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
        us.showTuple(new Tuple2<>(1234,"This is a tuple param"));
        UsersService users = us.getUsersService();
        System.out.println(users.getAllUsers());
        Optional<Integer> opt1  = users.testOptional(1);
        Optional<Integer> opt2  = users.testOptional(null);
        System.out.println("Opt1:" + opt1);
        System.out.println("Opt2:" + opt2);
    }


    static public void main(String...args){


        new TestRObjects().test();
    }
}
