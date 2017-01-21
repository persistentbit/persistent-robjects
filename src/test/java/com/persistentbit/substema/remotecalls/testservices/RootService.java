package com.persistentbit.substema.remotecalls.testservices;

import com.persistentbit.core.OK;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.substema.annotations.Remotable;
import com.persistentbit.substema.annotations.RemoteCache;

/**
 * TODOC
 *
 * @author petermuys
 * @since 20/01/17
 */
@Remotable
public interface RootService{

	@RemoteCache
	Result<String> remoteCachedString();

	@RemoteCache
	Result<String> remoteCachedLazyString();

	@RemoteCache
	Result<Integer> remoteCachedReturningNull();

	@RemoteCache
	Result<String> remoteCachedEmpty();


	Result<TestValue> getLazyPeter();

	Result<TestValue> getEls();

	Result<Tuple2<TestValue, TestValue>> getPeterAndEls();

	Result<TestValue> getEmptyValue();

	Result<OK> login(String userName);

	Result<LoggedInService> getLoggedInService();
}
