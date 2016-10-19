package com.persistentbit;

import com.persistentbit.core.logging.PLog;
import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.nodes.JJNode;
import com.persistentbit.jjson.nodes.JJParser;
import com.persistentbit.jjson.nodes.JJPrinter;
import com.persistentbit.substema.RCall;
import com.persistentbit.substema.RemoteService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;


public class MinimumServer {

    static class RemoteServiceHttphandler implements HttpHandler {
        static private PLog log = PLog.get(RemoteServiceHttphandler.class);
        private final JJMapper  mapper = new JJMapper();
        private final RemoteService service;

        public RemoteServiceHttphandler(RemoteService service) {
            this.service = service;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            try(Reader r = new InputStreamReader(t.getRequestBody(), Charset.forName("UTF-8"))){
                JJNode callNode = JJParser.parse(r);
                service.call(mapper.read(callNode, RCall.class))
                        .thenAccept(cr -> {
                            try{
                                JJNode callResultNode = mapper.write(cr);
                                String response = JJPrinter.print(false,callResultNode);
                                t.sendResponseHeaders(200, response.length());
                                try(OutputStream os = t.getResponseBody()) {
                                    os.write(response.getBytes());
                                }
                            }catch (Exception e){
                                throw new RuntimeException(e);
                            }


                        });
            }

        }
    }


/*
    public static void main(String[] args) throws Exception {
        BravoContext context = new BravoContext();

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new RemoteServiceHttphandler(context.server.get()));
        server.setExecutor(null); // creates a default executor
        server.start();
    }
*/

}
