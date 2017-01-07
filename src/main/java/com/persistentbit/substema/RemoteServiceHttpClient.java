package com.persistentbit.substema;

import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.IO;
import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.nodes.JJNode;
import com.persistentbit.jjson.nodes.JJParser;
import com.persistentbit.jjson.nodes.JJPrinter;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of a {@link RemoteService} that uses a HTTP server as endpoint.<br>
 * @author Peter Muys
 */
public class RemoteServiceHttpClient implements RemoteService{

    private final URL             url;
    private final JJMapper        mapper;
    private final ExecutorService executor;

    public RemoteServiceHttpClient(URL url) {
        this(url, ForkJoinPool.commonPool());
    }

    public RemoteServiceHttpClient(String url){
        this(url,ForkJoinPool.commonPool());
    }

    public RemoteServiceHttpClient(URL url,ExecutorService executor) {
        this(url,executor,new JJMapper());
    }

    public RemoteServiceHttpClient(String url,ExecutorService executor){
        this(url,executor,new JJMapper());
    }


    public RemoteServiceHttpClient(URL url,ExecutorService executor, JJMapper mapper){
        this.url = url;
        this.executor = executor;
        this.mapper = mapper;
    }
    public RemoteServiceHttpClient(String url,ExecutorService executor, JJMapper mapper){
        try {
            this.url = new URL(url);
            this.mapper = mapper;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.executor = executor;
    }


    @Override
    public Result<RCallResult> call(RCall call) {
        return Result.async(executor, () -> Result.function(call).code(l -> {
            JJNode callNode = mapper.write(call);
            //log.debug(() -> url + " call " + callNode);
            return doPost(callNode).map(resultNode -> {
                return mapper.read(resultNode, RCallResult.class);
            });
        }));
    }

    @Override
    public void close(long timeOut, TimeUnit timeUnit) {
        executor.shutdown();
        try {
            executor.awaitTermination(timeOut,timeUnit);
        } catch (InterruptedException e) {
            throw new RObjException(e);
        }
    }

    private Result<JJNode> doPost(JJNode content) {
        return Result.function(content).code(l -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                Writer w = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                JJPrinter.print(false, content, w);
                w.flush();
                return IO.readTextStream(connection.getInputStream(),IO.utf8).flatMap(data -> {
                    l.info("Do Post Result: " + data);
                    return JJParser.parse(data);
                });
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
            }
        });

    }

}
