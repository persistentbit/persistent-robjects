package com.persistentbit.robjects;

import com.persistentbit.robjects.rod.RodTokenType;
import com.persistentbit.robjects.rod.RodTokenizer;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by petermuys on 12/09/16.
 */
public class TestROD {
    @Test
    public void testRODParsing() {
        try{
            RodTokenizer tokenizer = new RodTokenizer();
            String txt = new String(Files.readAllBytes(Paths.get(RodTokenizer.class.getResource("/app.rod").toURI())));
            System.out.println(txt);
            tokenizer.tokenize("app.rod",txt).lazy().filter(t -> t.type != RodTokenType.tWhiteSpace).forEach(System.out::println);

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
