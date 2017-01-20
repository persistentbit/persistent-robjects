package com.persistentbit.substema.remotecalls.testservices;

import com.persistentbit.core.result.Result;

/**
 * TODOC
 *
 * @author petermuys
 * @since 20/01/17
 */
public class RootServiceImpl implements RootService{

	@Override
	public Result<String> remoteCachedString() {
		return Result.success("This is the remoteCachedString");
	}

	@Override
	public Result<String> remoteCachedLazyString() {
		return Result.lazy(() -> Result.success("This is the lazy remoteCachedLazyString"));
	}

}
