package com.persistentbit.substema.remotecalls.testservices;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.result.Result;
import com.persistentbit.substema.RSessionManager;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/01/17
 */
public class LoggedInServiceImpl implements LoggedInService{

	private final RSessionManager<RootServiceImpl.SessionData> sessionManager;

	public LoggedInServiceImpl(
		RSessionManager<RootServiceImpl.SessionData> sessionManager
	) {
		this.sessionManager = sessionManager;
	}

	@Override
	public Result<String> getLoginName() {
		return Result.fromOpt(sessionManager.getData()).map(sd -> sd.getLoginName());
	}

	@Override
	public Result<PList<TestValue>> getAllValues() {
		return Result.lazy(() -> Result.success(PList.val(RootServiceImpl.peter, RootServiceImpl.els)));
	}
}
