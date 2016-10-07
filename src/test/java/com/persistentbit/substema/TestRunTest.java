package com.persistentbit.substema;

import org.junit.Test;

/**
 * Created by petermuys on 18/09/16.
 */
public class TestRunTest {
    @Test
    public void generateCode() throws Exception{
        /*
        String rodFileName= "runtest.rod";
        String destPackage = "com.persistentbit.generated.runtest";
        URL url = SubstemaJavaGen.class.getResource("/" + rodFileName);
        System.out.println("URL: " + url);
        Path path = Paths.get(url.toURI());
        System.out.println("Path  = " + path);
        String rod = new String(Files.readAllBytes(path));
        SubstemaTokenizer tokenizer = new SubstemaTokenizer();
        PList<Token<SubstemaTokenType>> tokens = tokenizer.tokenize(rodFileName,rod);
        SubstemaParser parser = new SubstemaParser(destPackage,tokens);
        RSubstema service = parser.parseSubstema();
        System.out.println(service);
        PList<GeneratedJava> gen = SubstemaJavaGen.generate(new JavaGenOptions(true,false),destPackage,service);
        Path srcPath = SourcePath.findTestSourcePath(SubstemaJavaGen.class, rodFileName);

        Path packagePath = srcPath.toAbsolutePath().resolve(destPackage.replace('.', File.separatorChar));
        Files.createDirectories(packagePath);
        gen.forEach(gj -> {
            Path filePath = packagePath.resolve(gj.name.getClassName() + ".java");

            System.out.println("File: " + filePath.toString());
            try {
                Files.write(filePath,gj.code.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(gj.code);
            System.out.println("-----------------------------------");

        });
        */
    }

    @Test
    public void testRunServer() throws Exception{
        /*
        RemoteService rServer = new RServer<>("ThisIsTheSecret",RodTest.class,RodTestSessionData.class,(sm)-> new RodTestImpl(sm));
        rServer = new JSonRemoteService(rServer);
        //rServer = new RemoteServiceLogger(rServer);
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



        System.out.println("done");
        rServer.close();
        */
    }

}
