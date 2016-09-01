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

/**
 */
public class RemoteServiceHttpClient implements RemoteService{
    private final URL url;
    private final JJMapper mapper;

    public RemoteServiceHttpClient(URL url) {
        this(url,new JJMapper());
    }

    public RemoteServiceHttpClient(String url){
        this(url,new JJMapper());
    }
    public RemoteServiceHttpClient(URL url, JJMapper mapper){
        this.url = url;
        this.mapper = mapper;
    }
    public RemoteServiceHttpClient(String url,JJMapper mapper){
        try {
            this.url = new URL(url);
            this.mapper = mapper;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public RCallResult call(RCall call) {
        JJNode callNode = mapper.write(call);
        JJNode resultNode = doPost(callNode);
        return mapper.read(resultNode,RCallResult.class);
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
