package com.persistentbit.substema.remotecalls.testservices;

import com.persistentbit.core.OK;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.substema.RSessionManager;

import java.time.LocalDateTime;

/**
 * TODOC
 *
 * @author petermuys
 * @since 20/01/17
 */
public class RootServiceImpl implements RootService{

	public static class SessionData extends BaseValueClass{

		private final String loginName;

		public SessionData(String loginName) {
			this.loginName = loginName;
		}

		public String getLoginName() {
			return loginName;
		}
	}

	private final RSessionManager<SessionData> sessionManager;


	public RootServiceImpl(RSessionManager<SessionData> sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Override
	public Result<String> remoteCachedString() {
		return Result.success("This is the remoteCachedString");
	}

	@Override
	public Result<String> remoteCachedLazyString() {
		return Result.lazy(() -> Result.success("This is the lazy remoteCachedLazyString"));
	}

	@Override
	public Result<Integer> remoteCachedReturningNull() {
		return null;
	}

	@Override
	public Result<String> remoteCachedEmpty() {
		return Result.empty("RemoteCachedEmpty result");
	}

	public static final TestValue peter = new TestValue(1, new Name("Peter", null, "Muys"));
	public static final TestValue els   = new TestValue(2, new Name("Els", null, "Van Oost"));

	@Override
	public Result<TestValue> getLazyPeter() {
		return Result.lazy(() -> Result.success(peter));
	}

	@Override
	public Result<TestValue> getEls() {
		return Result.success(els);
	}

	@Override
	public Result<Tuple2<TestValue, TestValue>> getPeterAndEls() {
		return getLazyPeter().combine(getEls());
	}

	@Override
	public Result<TestValue> getEmptyValue() {
		return Result.empty("result from getEmptyValue()");
	}

	@Override
	public Result<OK> login(String userName) {
		if(userName == null) {
			return Result.failure("userName is null");
		}
		if(userName.equals("NotCorrect")) {
			return Result.failure("Login name can't be 'NotCorrect'");
		}
		sessionManager.setData(new SessionData(userName), LocalDateTime.now().plusMinutes(1));
		return OK.result;
	}

	@Override
	public Result<LoggedInService> getLoggedInService() {
		return Result.fromOpt(sessionManager.getData())
			.flatMapEmpty(e -> Result.failure("Not Logged In!!!"))
			.map(sd -> new LoggedInServiceImpl(sessionManager));
	}
}
