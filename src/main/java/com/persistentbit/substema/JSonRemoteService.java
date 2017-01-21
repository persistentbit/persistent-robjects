package com.persistentbit.substema;

import com.persistentbit.core.result.Result;
import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.nodes.JJNode;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Peter Muys
 * @since 30/08/2016
 */
public class JSonRemoteService implements RemoteService{
    static private final Logger log = Logger.getLogger(JSonRemoteService.class.getName());
    private RemoteService   service;
    private JJMapper        mapper;

    public JSonRemoteService(RemoteService service,JJMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }
    public JSonRemoteService(RemoteService service) {
        this(service,new JJMapper());
    }


    @Override
    public void close(long timeOut, TimeUnit timeUnit) {
        service.close();
    }

    @Override
    public Result<RCallResult> call(RCall call) {
        return Result.function(call).code(l -> {
            JJNode callNode = mapper.write(call);
            l.info("JSON call: " + callNode.toString());
            RCall               callFromJson   = mapper.read(callNode, RCall.class);
            Result<RCallResult> resRCallResult = service.call(callFromJson).completed();
            return resRCallResult.map(callResult -> {
                JJNode node = mapper.write(callResult);
                l.info("CallResult", node);
                RCallResult fromJson = mapper.read(node, RCallResult.class);
                return callResult;
            });
        });


    }

    @Override
    public String toString() {
        return "JSONRemoteService[" + service + "]";
    }
}
