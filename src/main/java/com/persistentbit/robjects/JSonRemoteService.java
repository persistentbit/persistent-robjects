package com.persistentbit.robjects;

import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.nodes.JJNode;
import com.persistentbit.jjson.nodes.JJPrinter;

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
    public RCallResult call(RCall call) {
        JJNode callNode = mapper.write(call);
        log.fine(() -> "CALL: " +JJPrinter.print(true,callNode));
        JJNode node = mapper.write(service.call(mapper.read(callNode,RCall.class)));
        log.fine(() -> "RESULT: " +JJPrinter.print(true,node));
        RCallResult callResult = mapper.read(node,RCallResult.class);
        return callResult;
    }
}
