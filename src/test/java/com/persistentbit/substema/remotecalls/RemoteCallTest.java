package com.persistentbit.substema.remotecalls;

import com.persistentbit.core.result.Result;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.substema.RProxy;
import com.persistentbit.substema.RServer;
import com.persistentbit.substema.SubstemaTestUtils;
import com.persistentbit.substema.remotecalls.testservices.LoggedInService;
import com.persistentbit.substema.remotecalls.testservices.RootService;
import com.persistentbit.substema.remotecalls.testservices.RootServiceImpl;
import com.persistentbit.substema.remotecalls.testservices.TestValue;

/**
 * TODOC
 *
 * @author petermuys
 * @since 20/01/17
 */
public class RemoteCallTest extends SubstemaTestUtils{

	private static final RootService createRemoteRootService() {
		RServer rserver = new RServer<>(
			"MySecret",
			RootService.class,
			RootServiceImpl.SessionData.class,
			sessionManager -> new RootServiceImpl(sessionManager)
		);
		return RProxy.create(rserver);
	}

	static final TestCase callCached = TestCase.name("Call Cached Remote values").code(tr -> {
		RootService    service = createRemoteRootService();
		Result<String> res     = service.remoteCachedString();
		tr.isTrue(res.isComplete());
		tr.isSuccess(res);
		tr.isEquals(res.orElseThrow(), "This is the remoteCachedString");

		res = service.remoteCachedLazyString();
		tr.isTrue(res.isComplete(), "All cached values should be completed");
		tr.isEquals(res.orElseThrow(), "This is the lazy remoteCachedLazyString");
		tr.isSuccess(res);
		tr.isTrue(res.isComplete());

		Result<Integer> ires = service.remoteCachedReturningNull();
		tr.isFailure(ires);

		tr.isEmpty(service.remoteCachedEmpty());
	});

	static final TestCase callSimple = TestCase.name("Call Non Cached values").code(tr -> {
		RootService       service = createRemoteRootService();
		Result<TestValue> peter   = service.getLazyPeter();
		tr.isFalse(peter.isComplete(), "A Method returning a lazy result should still be lazy");
		tr.isSuccess(peter);
		tr.isEquals(peter.orElseThrow(), RootServiceImpl.peter);
		Result<TestValue> els = service.getEls();
		tr.isTrue(els.isComplete());
		tr.isSuccess(els);
		tr.isEquals(els.orElseThrow(), RootServiceImpl.els);

		Result<Tuple2<TestValue, TestValue>> peterAndEls = service.getPeterAndEls();
		tr.isFalse(peterAndEls.isComplete());
		tr.isSuccess(peterAndEls);
		tr.isEquals(peterAndEls.orElseThrow(), Tuple2.of(RootServiceImpl.peter, RootServiceImpl.els));

		tr.isEmpty(service.getEmptyValue());
	});

	static final TestCase sessions = TestCase.name("Remote objects with sessions").code(tr -> {
		RootService service = createRemoteRootService();
		tr.isFailure(service.login("NotCorrect"));
		tr.isFailure(service.getLoggedInService());
		tr.isSuccess(service.login("Peter"));
		Result<LoggedInService> loggedIn = service.getLoggedInService();
		tr.isSuccess(loggedIn);
		tr.isEquals(loggedIn.flatMap(li -> li.getLoginName()).orElseThrow(), "Peter");
	});

	public static void main(String[] args) {
		new RemoteCallTest().testAll();
	}
}
