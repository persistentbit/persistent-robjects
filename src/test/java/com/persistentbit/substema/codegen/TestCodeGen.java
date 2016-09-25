package com.persistentbit.substema.codegen;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.sourcegen.SourcePath;
import com.persistentbit.core.tokenizer.Token;
import com.persistentbit.substema.javagen.GeneratedJava;
import com.persistentbit.substema.javagen.JavaGenOptions;
import com.persistentbit.substema.javagen.ServiceJavaGen;
import com.persistentbit.substema.rod.RodParser;
import com.persistentbit.substema.rod.RodTokenType;
import com.persistentbit.substema.rod.RodTokenizer;
import com.persistentbit.substema.rod.values.RSubstema;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by petermuys on 16/09/16.
 */
public class TestCodeGen {


    @Test
    public void testCodeGen() throws Exception{
        generateCode(new JavaGenOptions(),"com.persistentbit.generated.defaultOptions");
        generateCode(new JavaGenOptions().withGenerateGetters(true),"com.persistentbit.generated.withGetters");
        generateCode(new JavaGenOptions().withGenerateUpdaters(true),"com.persistentbit.generated.withUpdaters");
        generateCode(new JavaGenOptions().withGenerateUpdaters(true).withGenerateGetters(true),"com.persistentbit.generated.generateAll");


    }

    public void generateCode(JavaGenOptions options,String destPackage) throws Exception{
        String rodFileName= "codeGenTest.rod";
        URL url = ServiceJavaGen.class.getResource("/" + rodFileName);
        System.out.println("URL: " + url);
        Path path = Paths.get(url.toURI());
        System.out.println("Path  = " + path);
        String rod = new String(Files.readAllBytes(path));
        RodTokenizer tokenizer = new RodTokenizer();
        PList<Token<RodTokenType>> tokens = tokenizer.tokenize(rodFileName,rod);
        RodParser parser = new RodParser(destPackage,tokens);
        RSubstema service = parser.parseSubstema();
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
}
