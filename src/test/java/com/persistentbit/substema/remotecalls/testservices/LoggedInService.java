package com.persistentbit.substema.remotecalls.testservices;

import com.persistentbit.core.result.Result;
import com.persistentbit.substema.annotations.Remotable;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/01/17
 */
@Remotable
public interface LoggedInService{

	Result<String> getLoginName();
}
