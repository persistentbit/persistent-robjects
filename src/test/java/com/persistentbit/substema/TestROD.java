package com.persistentbit.substema;

import com.persistentbit.substema.compiler.SubstemaTokenType;
import com.persistentbit.substema.compiler.SubstemaTokenizer;
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
            SubstemaTokenizer tokenizer = new SubstemaTokenizer();
            String            txt       =
                new String(Files.readAllBytes(Paths.get(SubstemaTokenizer.class.getResource("/app.substema").toURI())));
            System.out.println(txt);
            tokenizer.tokenize("app.rod",txt).lazy().filter(t -> t.type != SubstemaTokenType.tWhiteSpace).forEach(System.out::println);

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
