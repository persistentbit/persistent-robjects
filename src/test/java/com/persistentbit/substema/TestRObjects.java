package com.persistentbit.substema;

import com.persistentbit.jjson.mapping.JJMapper;
import org.junit.Test;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public void test() throws Exception{
        JJMapper mapper = new JJMapper();
        /*
        new AppImpl(mapper,new JJSigning("testAppSecret"));
        RemoteService rserver = new RServer<>("Dit is het geheim",App.class,Integer.class,(sm) -> new AppImpl(mapper,new JJSigning("testAppSecret")));
        rserver = new JSonRemoteService(rserver);
        App app = RProxy.create(rserver);
        AppVersion version = app.getVersion().get();
        System.out.println(app.getVersion());
        assert version.name.equals("RName");
        assert version.environment == AppVersion.RunEnvironment.develop;
        String lt = app.createLoginToken("notok","notok").get();
        assert lt == null;
        final String un = "testPeterMuys";
        lt = app.createLoginToken(un,"testPassword").get();
        assert lt != null;
        UserSession us = app.getUserSession("wrongToken").get();
        assert us == null;
        us = app.getUserSession(lt).get();
        assert us != null;
        UserData ud = us.getDetails().get();
        assert ud.userName.equals(un);
        us.testWithVoid();
        System.out.println("Got Tuple: " + us.createMeATuple(1,"test"));
        us.showTuple(new Tuple2<>(1234,"This is a tuple param"));
        UsersService users = us.getUsersService().get();
        System.out.println(users.getAllUsers());
        Optional<Integer> opt1  = users.testOptional(1).get();
        Optional<Integer> opt2  = users.testOptional(null).get();
        System.out.println("Opt1:" + opt1);
        System.out.println("Opt2:" + opt2);
        assert opt1.isPresent();
        assert opt1.get() == 1;
        assert opt2.isPresent() == false;
        */
    }



    static public void main(String...args) throws Exception{


        new TestRObjects().test();
    }
}
