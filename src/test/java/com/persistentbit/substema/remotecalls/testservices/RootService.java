package com.persistentbit.substema.remotecalls.testservices;

import com.persistentbit.core.result.Result;
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
}
