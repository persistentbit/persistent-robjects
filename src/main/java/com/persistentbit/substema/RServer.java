package com.persistentbit.substema;


import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.logging.PLog;
import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.nodes.JJParser;
import com.persistentbit.jjson.nodes.JJPrinter;
import com.persistentbit.jjson.utils.ObjectWithTypeName;
import com.persistentbit.substema.annotations.RemoteCache;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;


/**
 * Implements a {@link RemoteService} using a Java class implementation
 * @param <R> The Service Root interface type
 * @param <SESSION> The Session data type
 */
public class RServer<R,SESSION> implements RemoteService{

	private static final PLog log = PLog.get(RServer.class);
	private final Class<R>   rootInterface;
    private final Class<SESSION>    sessionClass;
    private final Function<RSessionManager<SESSION>,R> rootSupplier;
    private final JJMapper  mapper;
    private final String    secret;
    private final ExecutorService executor;


    public RServer(String secret,Class<R> rootInterface, Class<SESSION> sessionClass, Function<RSessionManager<SESSION>,R> rootSupplier){
        this(secret, rootInterface,sessionClass,rootSupplier, ForkJoinPool.commonPool(),new JJMapper());
    }

    public RServer(String secret,Class<R> rootInterface, Class<SESSION> sessionClass, Function<RSessionManager<SESSION>,R> rootSupplier,ExecutorService executor){
        this(secret, rootInterface,sessionClass,rootSupplier,executor,new JJMapper());
    }
    public RServer(String secret, Class<R> rootInterface, Class<SESSION> sessionClass, Function<RSessionManager<SESSION>,R> rootSupplier,ExecutorService executor,JJMapper mapper) {
        this.secret = secret;
        this.rootInterface = Objects.requireNonNull(rootInterface);
        this.sessionClass = Objects.requireNonNull(sessionClass);
        this.rootSupplier = Objects.requireNonNull(rootSupplier);
        this.executor = executor;
        this.mapper = mapper;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public void close(long timeOut, TimeUnit timeUnit) {
        executor.shutdown();
        try {
            executor.awaitTermination(timeOut,timeUnit);
        } catch (InterruptedException e) {
			log.error(e.getMessage());

            throw new RObjException(e);
        }
    }

	@Override
	@SuppressWarnings("unchecked")
	public CompletableFuture<RCallResult>  call(RCall call){

        // First we get the sessionData from the call.
        return CompletableFuture.supplyAsync(() -> {
            SESSION sessionData = null;
            LocalDateTime sessionExpires = null;

            if(call.getSessionData() != null){
                RSessionData data = call.getSessionData();
                if(data.verifySignature(secret) == false){
                    throw new RObjException("Invalid Session signature");
                }
                sessionData = mapper.read(JJParser.parse(new String(Base64.getDecoder().decode(data.data))),sessionClass);
                sessionExpires =data.validUntil;
                if(sessionExpires.isBefore(LocalDateTime.now())){
                    //The Session Data is expired, so we continue with no sessionData.
                    //It is up to the implementation to check if there is a session.
					log.warn("SESSION EXPIRED: " + sessionData);
					sessionData = null;
                    sessionExpires = null;
                }
            }

            //Create The session manager that is used
            //For the complete implementation call chain
            RSessionManager<SESSION> sessionManager = new RSessionManager<>(sessionData,sessionExpires);

            if(call.getThisCall() == null){
                //This is a call to get the Root Object.
                RemoteObjectDefinition rod =  createROD(RCallStack.createAndSign(PList.empty(),mapper,secret),this.rootInterface,rootSupplier.apply(sessionManager));
                return RCallResult.robject(getSession(sessionManager),rod);
            }


            try {
                Object result = call(rootSupplier.apply(sessionManager),call.getCallStack());
                result = singleCall(result, call.getThisCall());
                Object resultNoOption = result;
                if(result instanceof Optional){
                    resultNoOption = ((Optional)result).orElseGet(null);
                }
                Class<?> remoteClass = RemotableClasses.getRemotableClass(resultNoOption.getClass());

                if(remoteClass == null ){
                    return RCallResult.value(getSession(sessionManager),call.getThisCall().getMethodToCall(),result);
                } else {
                    RCallStack newCallStack = RCallStack.createAndSign(call.getCallStack().getCallStack().plus(call.getThisCall()),mapper,secret);
                    return RCallResult.robject(getSession(sessionManager),createROD(newCallStack,remoteClass,resultNoOption));
                }
            }catch (Exception e){
				log.error(e);
				return RCallResult.exception(getSession(sessionManager),e);
            }

        },executor);

    }

    private RSessionData    getSession(RSessionManager<SESSION> sessionManager){
        if(sessionManager.getData().isPresent() == false){
            return null;
        }
		SESSION sessionData = sessionManager.getData().get();
		String  data        =
			Base64.getEncoder().encodeToString(JJPrinter.print(false, mapper.write(sessionData)).getBytes());
		return new RSessionData(data, sessionManager.getExpires().get()).signed(secret);
	}

    private RemoteObjectDefinition  createROD(RCallStack call, Class<?> remotableClass, Object obj){
        try {
            PList<MethodDefinition> remoteMethods = PList.empty();
            PMap<MethodDefinition, ObjectWithTypeName> cachedMethods = PMap.empty();
            for (Method m : remotableClass.getDeclaredMethods()) {
                MethodDefinition md = new MethodDefinition(remotableClass,m);
                if (m.getParameterCount() == 0 && m.getDeclaredAnnotation(RemoteCache.class) != null) {
                    Object value = m.invoke(obj);
                    try{
						value = ((Future) value).get();
					}catch (Exception e){
						log.error(e.getMessage());
						throw new RuntimeException("Error getting cached value from " + remotableClass.getName() + " method: " + md.getMethodName());
                    }
                    cachedMethods = cachedMethods.put(md, new ObjectWithTypeName(value));
                } else {
                    remoteMethods = remoteMethods.plus(md);
                }
            }
            return new RemoteObjectDefinition(remotableClass,remoteMethods, cachedMethods,call);
        } catch (Exception e){
			log.error(e);
			throw new RuntimeException(e);
        }

    }

	@SuppressWarnings("unchecked")
	private Object singleCall(Object obj, RMethodCall call) {
		MethodDefinition md = call.getMethodToCall();
        if(obj == null){
            throw new RuntimeException("Can't call on null: " + md);
        }
        try {
            Method m = obj.getClass().getMethod(md.getMethodName(), md.getParamTypes());
            CompletableFuture<Object> methodResult = (CompletableFuture<Object>) m.invoke(obj, call.getArguments());
            if(methodResult == null){
                //We got a null instead of a CompletableFuture.
                throw new RObjException("method did not return a CompletableFuture: " + m);
            }
            return methodResult.get();
        }catch(Exception e){
			log.error(e.getMessage());
			throw new RObjException(e);
        }

    }

	private Object call(Object obj, RCallStack callStack) {
		if(callStack.verifySignature(secret,mapper) == false){
             throw new RObjException("Wrong signature !!!");
        }
        for(RMethodCall c : callStack.getCallStack()){
            obj = singleCall(obj,c);
        }
        return obj;
    }


}
