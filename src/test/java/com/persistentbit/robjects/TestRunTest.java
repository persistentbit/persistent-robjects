package com.persistentbit.robjects;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.sourcegen.SourcePath;
import com.persistentbit.core.tokenizer.Token;
import com.persistentbit.generated.runtest.RodTest;
import com.persistentbit.jjson.mapping.JJMapper;
import com.persistentbit.robjects.javagen.GeneratedJava;
import com.persistentbit.robjects.javagen.JavaGenOptions;
import com.persistentbit.robjects.javagen.ServiceJavaGen;
import com.persistentbit.robjects.rod.RodParser;
import com.persistentbit.robjects.rod.RodTokenType;
import com.persistentbit.robjects.rod.RodTokenizer;
import com.persistentbit.robjects.rod.values.RService;
import com.persistentbit.robjects.runtestimpl.RodTestImpl;
import com.persistentbit.robjects.runtestimpl.RodTestSessionData;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Created by petermuys on 18/09/16.
 */
public class TestRunTest {
    @Test
    public void generateCode() throws Exception{
        String rodFileName= "runtest.rod";
        String destPackage = "com.persistentbit.generated.runtest";
        URL url = ServiceJavaGen.class.getResource("/" + rodFileName);
        System.out.println("URL: " + url);
        Path path = Paths.get(url.toURI());
        System.out.println("Path  = " + path);
        String rod = new String(Files.readAllBytes(path));
        RodTokenizer tokenizer = new RodTokenizer();
        PList<Token<RodTokenType>> tokens = tokenizer.tokenize(rodFileName,rod);
        RodParser parser = new RodParser(destPackage,tokens);
        RService service = parser.parseService();
        System.out.println(service);
        PList<GeneratedJava> gen = ServiceJavaGen.generate(new JavaGenOptions(),destPackage,service);
        Path srcPath = SourcePath.findTestSourcePath(ServiceJavaGen.class, rodFileName);

        Path packagePath = srcPath.toAbsolutePath().resolve(destPackage.replace('.', File.separatorChar));
        Files.createDirectories(packagePath);
        gen.forEach(gj -> {
            Path filePath = packagePath.resolve(gj.name.className + ".java");

            System.out.println("File: " + filePath.toString());
            try {
                Files.write(filePath,gj.code.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(gj.code);
            System.out.println("-----------------------------------");

        });
    }

    @Test
    public void testRunServer() throws Exception{
        RemoteService rServer = new RServer<>("ThisIsTheSecret",RodTest.class,RodTestSessionData.class,(sm)-> new RodTestImpl(sm));
        rServer = new JSonRemoteService(rServer);
        RodTest root = RProxy.create(rServer);
        root.getInfo().thenAccept(info -> {
            System.out.println("info:" + new JJMapper().write(info));

        });
        root.login("mup","pw").thenCompose(user -> {
            System.out.println("Login user = " + new JJMapper().write(user));
            return root.getUserSession();
        }).thenCompose(optUser -> {
            System.out.println("Got User Session " + optUser);
            return optUser.get().getUser();
        }).thenAccept(user -> {
            System.out.println("Got user from user session: " + user);

        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });

        /*root.getUserSession().thenCompose(us -> us.get().getUser())
                .thenAccept(user -> {
                    System.out.println("Got User from UserSession");
                });*/

        System.out.println("done");
        rServer.close();

    }

}
