package com.persistentbit.substema;

import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Result;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * An RProxy is a Interface Proxy for Remote Objects that uses a {@link RemoteService} to
 * execute the method calls.<br>
 * Usage:<br>
 * {@code
 *      RemoteService remoteService = ...
 * 		SomeServiceInterface service = RProxy.create(remoteService);
 * 	    //We now have a service instance that automatically uses the RemoteService instance
 * }
 *
 *
 * @author Peter Muys
 * @see RemoteService
 */
public final class RProxy implements InvocationHandler{


	private final RemoteService          server;
	private final RemoteObjectDefinition rod;

	private static class ClientSessionData{

		private RSessionData sessionData;

		public RSessionData getSessionData() {
			return sessionData;
		}

		public void setSessionData(RSessionData sessionData) {
			this.sessionData = sessionData;
		}

	}

	private final ClientSessionData clientSessionData;


	private RProxy(RemoteService server, ClientSessionData clientSessionData, RemoteObjectDefinition rod) {
		this.server = server;
		this.clientSessionData = clientSessionData;
		this.rod = rod;

	}


	/**
	 * Create a new Proxy for the root Remote Object, using the given {@link RemoteService} to execute the calls.
	 *
	 * @param server The RemoteService
	 * @param <C>    The type of the Root Remote Object
	 *
	 * @return The Proxy
	 */
	public static <C> C create(RemoteService server) {
		return Log.function(server).code(l -> {
			return create(server, new ClientSessionData(), server.getRoot().orElseThrow().getRod().get().orElseThrow());
		});

	}


	/**
	 * Create a new Proxy for the given {@link RemoteObjectDefinition}.<br>
	 * The new proxy will share the {@link ClientSessionData} with this proxy.<br>
	 *
	 * @param server            The RemoteService
	 * @param clientSessionData The SessionData (originated from the root service proxy)
	 * @param rod               The Remote Object Definition for this proxy
	 * @param <C>               The Result interface type
	 *
	 * @return A new Interface proxy
	 */
	private static <C> C create(RemoteService server, ClientSessionData clientSessionData, RemoteObjectDefinition rod) {
		//noinspection unchecked
		return (C) Proxy.newProxyInstance(
			RProxy.class.getClassLoader(),
			new Class<?>[]{rod.getRemoteObjectClass()},
			new RProxy(server, clientSessionData, rod)
		);
	}


	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		//IGNORE toString method
		if(method.getName().equals("toString")) {
			return "RProxy[" + server + "]";
		}
		return Result.function(method.getName()).code(l -> {


			MethodDefinition md = new MethodDefinition(rod.getRemoteObjectClass(), method);
			l.info("Calling " + md.toString());



			if(rod.getRemoteCached().containsKey(md)) {
				Result<Object> cached = rod.getRemoteCached().get(md);
				return cached;
			}
			//Create The Call
			RCall call = new RCall(clientSessionData.getSessionData(), rod.getCallStack(), new RMethodCall(md, args));

			//Execute the Call
			return server.call(call).completed()
				.flatMap(callResult -> {
					clientSessionData.setSessionData(callResult.getSessionData().orElse(null));

					//If the result is a remote object,
					//Then create a new Proxy and return the remote object.
					if(callResult.getRod().isPresent()) {
						Object remResult = callResult
							.getRod().get()
							.map(rod -> RProxy.create(server, clientSessionData, rod));
						return (Result) remResult;
					}
					//Must be a result value
					return callResult.getResult().orElse(null);
				});

		});
	}

	@Override
	public String toString() {
		return "[Remote Proxy for " + server + "]";
	}
}
