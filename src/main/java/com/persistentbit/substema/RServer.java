package com.persistentbit.substema;


import com.persistentbit.core.Nothing;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Result;
import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.nodes.JJParser;
import com.persistentbit.jjson.nodes.JJPrinter;
import com.persistentbit.substema.annotations.RemoteCache;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


/**
 * Implements a {@link RemoteService} using a Java class implementation
 *
 * @param <R>       The Service Root interface type
 * @param <SESSION> The Session data type
 */
public class RServer<R, SESSION> implements RemoteService{

	private final Class<R>                              rootInterface;
	private final Class<SESSION>                        sessionClass;
	private final Function<RSessionManager<SESSION>, R> rootSupplier;
	private final JJMapper                              mapper;
	private final String                                secret;
	private final ExecutorService                       executor;


	public RServer(String secret, Class<R> rootInterface, Class<SESSION> sessionClass,
				   Function<RSessionManager<SESSION>, R> rootSupplier
	) {
		this(secret, rootInterface, sessionClass, rootSupplier, ForkJoinPool.commonPool(), new JJMapper());
	}

	public RServer(String secret, Class<R> rootInterface, Class<SESSION> sessionClass,
				   Function<RSessionManager<SESSION>, R> rootSupplier, ExecutorService executor
	) {
		this(secret, rootInterface, sessionClass, rootSupplier, executor, new JJMapper());
	}

	public RServer(String secret, Class<R> rootInterface, Class<SESSION> sessionClass,
				   Function<RSessionManager<SESSION>, R> rootSupplier, ExecutorService executor, JJMapper mapper
	) {
		this.secret = secret;
		this.rootInterface = Objects.requireNonNull(rootInterface);
		this.sessionClass = Objects.requireNonNull(sessionClass);
		this.rootSupplier = Objects.requireNonNull(rootSupplier);
		this.executor = executor;
		this.mapper = mapper;
	}

	@Override
	public String toString() {
		return "RServer[" + rootInterface.getName() + "]";
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	@Override
	public void close(long timeOut, TimeUnit timeUnit) {
		executor.shutdown();
		Log.function(timeOut, timeUnit).code(l -> {
			executor.awaitTermination(timeOut, timeUnit);
			return Nothing.inst;
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public Result<RCallResult> call(RCall call) {
		return Result.async(executor, () -> Result.function(call).code(l -> {
			SESSION       sessionData    = null;
			LocalDateTime sessionExpires = null;

			if(call.getSessionData() != null) {
				RSessionData data = call.getSessionData();
				if(data.verifySignature(secret) == false) {
					Result.failure("Invalid Session signature");
				}
				sessionData =
					mapper.read(JJParser.parse(new String(Base64.getDecoder().decode(data.data)))
									.orElseThrow(), sessionClass);
				sessionExpires = data.validUntil;
				if(sessionExpires.isBefore(LocalDateTime.now())) {
					//The Session Data is expired, so we continue with no sessionData.
					//It is up to the implementation to check if there is a session.
					l.warning("SESSION EXPIRED: " + sessionData);
					sessionData = null;
					sessionExpires = null;
				}
			}

			//Create The session manager that is used
			//For the complete implementation call chain
			RSessionManager<SESSION> sessionManager = new RSessionManager<>(sessionData, sessionExpires);
			RMethodCall              thisCall       = call.getThisCall();
			if(thisCall == null) {
				//This is a call to get the Root Object.
				return createROD(
					RCallStack.createAndSign(PList.empty(), mapper, secret),
					this.rootInterface,
					rootSupplier.apply(sessionManager)
				).map(rod ->
						  RCallResult.forRootRemoteObject(getSession(sessionManager), Result.success(rod))
				);
			}

			//Execute the call stack
			Result<Object> result =
				call(rootSupplier.apply(sessionManager), call.getCallStack())
					.flatMap(impl -> singleCall(impl, thisCall))//Execute this call
				;

			RSessionData resultSession = getSession(sessionManager);

			if(result.isError()) {
				return Result.success(RCallResult.forResultValue(thisCall.getMethodToCall(), resultSession, result));
			}
			boolean isRemotableResult = call.getThisCall().getMethodToCall().returnsRemotable();

			if(isRemotableResult) {

				//We have a remote object
				if(result.isEmpty()) {
					return Result.success(
						RCallResult.forRemoteObject(
							thisCall.getMethodToCall(),
							resultSession,
							Result
								.empty("Can't create a Remote Object definition for a null object in call " + thisCall)
						)
					);
				}
				Object remotableObjectImpl = result.orElseThrow();
				RCallStack newCallStack = RCallStack
					.createAndSign(call.getCallStack().getCallStack().plus(thisCall), mapper, secret);

				Result<RemoteObjectDefinition> resultRod =
					createROD(newCallStack, RemotableClasses
						.getRemotableClass(remotableObjectImpl.getClass()), remotableObjectImpl);

				return Result.success(
					RCallResult.forRemoteObject(
						thisCall.getMethodToCall(),
						resultSession,
						resultRod
					)
				);
			}
			//It must be a value result
			return Result.success(
				RCallResult.forResultValue(
					thisCall.getMethodToCall(),
					resultSession,
					result
				)
			);

		}));
	}

	private RSessionData getSession(RSessionManager<SESSION> sessionManager) {
		if(sessionManager.getData().isPresent() == false) {
			return null;
		}
		SESSION sessionData = sessionManager.getData().get();
		String data =
			Base64.getEncoder().encodeToString(JJPrinter.print(false, mapper.write(sessionData)).getBytes());
		return new RSessionData(data, sessionManager.getExpires().get()).signed(secret);
	}

	private Result<RemoteObjectDefinition> createROD(RCallStack call, Class<?> remotableClass, Object obj) {
		return Result.function(call, remotableClass, obj).code(l -> {
			PList<MethodDefinition>        remoteMethods = PList.empty();
			PMap<MethodDefinition, Result> cachedMethods = PMap.empty();
			for(Method m : remotableClass.getDeclaredMethods()) {
				MethodDefinition md = new MethodDefinition(remotableClass, m);

				if(m.getParameterCount() == 0 && m.getDeclaredAnnotation(RemoteCache.class) != null) {
					l.info("Getting cached value for " + md);
					Result<Object> value;
					try {
						value = (Result<Object>) m.invoke(obj);
					} catch(Exception e) {
						return Result.failure(new RuntimeException("Error getting cached value from " + remotableClass
							.getName() + " method: " + md.getMethodName(), e));
					}
					if(value == null) {
						value = Result.failure("Got a null as Result for the cached value method " + md);
					}
					value = value.completed();
					cachedMethods = cachedMethods.put(md, value);
				}
				else {
					remoteMethods = remoteMethods.plus(md);
				}
			}
			return Result.success(new RemoteObjectDefinition(remotableClass, remoteMethods, cachedMethods, call));
		});

	}

	@SuppressWarnings("unchecked")
	private Result<Object> singleCall(Object implementationObject, RMethodCall call) {
		return Result.function(implementationObject, call).code(l -> {
			MethodDefinition md = call.getMethodToCall();
			if(implementationObject == null) {
				return Result.failure("Can't call on a null implementation object: " + md);
			}
			Method m =
				implementationObject.getClass().getMethod(md.getMethodName(), md.getParamTypes());
			return (Result<Object>) m.invoke(implementationObject, call.getArguments());
		});
	}

	private Result<Object> call(Object implementationObject, RCallStack callStack) {
		return Result.function(implementationObject, callStack).code(l -> {
			if(callStack.verifySignature(secret, mapper) == false) {
				return Result.failure(new RObjException("Wrong signature !!! "));
			}
			Object resObj = implementationObject;
			for(RMethodCall c : callStack.getCallStack()) {
				if(resObj == null) {
					return Result.failure("Can't execute call on a null object implementation");
				}
				Result<Object> callResult = singleCall(resObj, c);
				callResult.withLogs(logs -> l.add(logs));
				if(callResult.isError()) {
					return callResult;
				}
				resObj = callResult.orElse(null);
			}
			return Result.result(resObj);
		});

	}


}
