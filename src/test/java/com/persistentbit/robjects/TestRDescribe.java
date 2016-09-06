package com.persistentbit.robjects;

import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.nodes.JJPrinter;
import com.persistentbit.robjects.describe.RemoteDescriber;
import com.persistentbit.robjects.describe.RemoteServiceDescription;
import com.persistentbit.robjects.testapi.App;
import org.junit.Test;

/**
 * @author Peter Muys
 * @since 6/09/2016
 */
public class TestRDescribe {
    @Test
    public void test() {
        RemoteServiceDescription des = new RemoteDescriber().descripeRemoteService(App.class);
        System.out.println(JJPrinter.print(true,new JJMapper().write(des)));
    }
}
