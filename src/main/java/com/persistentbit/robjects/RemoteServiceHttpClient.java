package com.persistentbit.robjects;

import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.nodes.JJNode;
import com.persistentbit.jjson.nodes.JJParser;
import com.persistentbit.jjson.nodes.JJPrinter;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.*;

/**
 */
public class RemoteServiceHttpClient implements RemoteService{
    private final URL url;
    private final JJMapper mapper;
    private ExecutorService executor;

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
    public CompletableFuture<RCallResult> call(RCall call) {
        return CompletableFuture.supplyAsync(() -> {
            JJNode callNode = mapper.write(call);
            JJNode resultNode = doPost(callNode);
            return mapper.read(resultNode,RCallResult.class);
        },executor);

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

    private JJNode doPost(JJNode content){
        HttpURLConnection connection = null;
        try{
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            try (Writer w = new OutputStreamWriter(connection.getOutputStream())) {
                JJPrinter.print(false, content, w);
                w.flush();
            }
            try(Reader rin = new InputStreamReader(connection.getInputStream())){
                JJNode json = JJParser.parse(rin);

                return json;
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }finally
        {
            if(connection != null){
                connection.disconnect();
            }
        }
    }

}
