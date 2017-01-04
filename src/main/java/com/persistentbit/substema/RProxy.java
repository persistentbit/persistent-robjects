package com.persistentbit.substema;

import com.persistentbit.core.logging.Log;
import com.persistentbit.core.utils.ReflectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
			return create(server, new ClientSessionData(), server.getRoot().orElseThrow().getRobject().get());
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
			return "RemoteObject[" + rod.getRemoteObjectClass().getName() + "]";
		}

		MethodDefinition md = new MethodDefinition(rod.getRemoteObjectClass(), method);
		if(rod.getRemoteCached().containsKey(md)) {
			Object cached = rod.getRemoteCached().get(md).getValue();
			return CompletableFuture.completedFuture(cached);
		}
		//Create The Call
		RCall call = new RCall(clientSessionData.getSessionData(), rod.getCallStack(), new RMethodCall(md, args));

		//Execute the Call
		return server.call(call)
			.map(result -> {

				//Save the new Session Data
				clientSessionData.setSessionData(result.getSessionData().orElse(null));

				//If the result is an exception: Throw it.
				result.getException().ifPresent(e -> { throw new RuntimeException("Remote exception", e);});

				//If the result is a value: Return it;
				if(result.getValue().isPresent()) {
					return result.getValue().get();
				}

				//Must be remote object
				RemoteObjectDefinition rod        = result.getRobject().orElse(null);
				Object                 remResult  = rod == null ? null : RProxy.create(server, clientSessionData, rod);
				ParameterizedType      computable = (ParameterizedType) method.getGenericReturnType();

				if(ReflectionUtils.classFromType(computable.getActualTypeArguments()[0]) == Optional.class) {
					remResult = Optional.ofNullable(remResult);
				}
				return remResult;

			});

	}
}
