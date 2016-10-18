package com.persistentbit;

import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.jjson.nodes.JJNode;
import com.persistentbit.jjson.nodes.JJParser;
import com.persistentbit.jjson.nodes.JJPrinter;
import com.persistentbit.substema.RCall;
import com.persistentbit.substema.RCallResult;
import com.persistentbit.substema.RemoteService;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * User: petermuys
 * Date: 31/10/15
 * Time: 11:22
 */
public class HttpRemoteServiceClient implements RemoteService{
    private static final Logger log = Logger.getLogger(HttpRemoteServiceClient.class.getName());

    private final URL url;
    private JJMapper mapper = new JJMapper();

    public HttpRemoteServiceClient(URL url){
        this.url = url;
    }
    public HttpRemoteServiceClient(String url){
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<RCallResult> call(RCall call) {
        JJNode callNode = this.mapper.write(call);
        return doPost(callNode).thenApply(node -> {
            RCallResult callResult = (RCallResult)this.mapper.read(node, RCallResult.class);
            return callResult;
        });
    }

    @Override
    public void close(long l, TimeUnit timeUnit) {

    }

    public CompletableFuture<JJNode> doPost(JJNode content){
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection connection = null;
            try{
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                if(content != null) {
                    connection.setDoOutput(true);
                    try (Writer w = new OutputStreamWriter(connection.getOutputStream())) {
                        JJPrinter.print(false, content, w);
                        w.flush();
                    }
                }
                try(Reader rin = new InputStreamReader(connection.getInputStream())){
                    JJNode json = JJParser.parse(rin);
                    //System.out.println("RESULT FROM CALL: " +  JJPrinter.print(true,json));
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
        });

    }

}
