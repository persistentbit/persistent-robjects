package com.persistentbit.robjects;

import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.nodes.JJNode;
import com.persistentbit.jjson.nodes.JJPrinter;

/**
 * @author Peter Muys
 * @since 30/08/2016
 */
public class JSonRemoteService implements RemoteService{
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
    public RCallResult getRoot() {
        RCallResult cr = service.getRoot();
        JJNode node = mapper.write(service.getRoot());
        System.out.println("root:" + JJPrinter.print(true,node));
        RCallResult res =  mapper.read(node,RCallResult.class);
        return res;
    }

    @Override
    public RCallResult call(RCall call) {
        JJNode callNode = mapper.write(call);
        JJNode node = mapper.write(service.call(mapper.read(callNode,RCall.class)));
        System.out.println("call:" + callNode);
        System.out.println("Result:" + node);
        RCallResult callResult = mapper.read(node,RCallResult.class);
        return callResult;
    }
}
