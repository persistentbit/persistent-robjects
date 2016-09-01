package com.persistentbit.robjects.testapi;

import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.nodes.JJNode;
import com.persistentbit.jjson.security.JJSigning;

import java.text.DateFormat;
import java.util.Date;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
public class AppImpl implements App{
    private JJMapper mapper;
    private JJSigning signing;
    public AppImpl(JJMapper mapper, JJSigning signing){
        this.signing = signing;
        this.mapper = mapper;
    }

    @Override
    public AppVersion getVersion() {
        return new AppVersion("RName","1.1.5-" + DateFormat.getDateTimeInstance().format(new Date()), AppVersion.RunEnvironment.develop);
    }

    private static class LoginToken{
        public final Date   created;
        public final String userName;

        public LoginToken(Date created, String userName) {
            this.created = created;
            this.userName = userName;
        }
    }

    @Override
    public String createLoginToken(String un, String pw) {
        if(un.startsWith("test") && pw.startsWith("test")){
            return signing.signAsString(mapper.write(new LoginToken(new Date(),un)));
        }
        return null;
    }

    @Override
    public UserSession getUserSession(String loginToken) {
        try{

            if(loginToken == null){
                return null;
            }
            JJNode node = signing.unsignedFromString(loginToken).orElse(null);
            if(node == null){
                return null;
            }
            LoginToken t = mapper.read(node,LoginToken.class);
            return new UserSessionImpl(t.userName);

        } catch(Exception e){
            //e.printStackTrace();
            return null;
        }
    }
}
