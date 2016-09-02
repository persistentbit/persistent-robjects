package com.persistentbit.robjects.testapi;

import com.persistentbit.core.Tuple2;
import com.persistentbit.robjects.annotations.Remotable;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
@Remotable
public interface UserSession {
    UserData    getDetails();

    Tuple2<Integer,String>  createMeATuple(int v1, String v2);

    void testWithVoid();
    void showTuple(Tuple2<Integer,String> tupleParam);
}
