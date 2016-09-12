package com.persistentbit.robjects.rod;

import com.persistentbit.core.Tuple2;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.tokenizer.SimpleTokenizer;
import com.persistentbit.core.utils.BaseValueClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.SimpleTimeZone;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.persistentbit.robjects.rod.RodTokenType.*;

/**
 * Created by petermuys on 12/09/16.
 */
public class RodTokenizer extends SimpleTokenizer<RodTokenType>{

    public RodTokenizer(){
        add("/\\*.*\\*/",tComment);
        add("\\n",tNl);
        add("\\(",tOpen);
        add("\\)",tClose);
        add("\\.",tPoint);
        add("package",tPackage);
        add("from",tFrom);
        add("class",tClass);
        add("import",tImport);
        add("cached",tCached);
        add("enum",tEnum);
        add("<",tGenStart);
        add(">",tGenEnd);
        add("\\,",tComma);
        add("\\?",tQuestion);
        add("\\:",tColon);
        add("\\;",tSemiColon);
        /*
         */
        add("[a-zA-Z_][a-zA-Z0-9_]*",tIdentifier);
        add("\\s+",tWhiteSpace);

    }

    static public void main(String...args){
        try{
            RodTokenizer tokenizer = new RodTokenizer();
            String txt = new String(Files.readAllBytes(Paths.get(RodTokenizer.class.getResource("/app.rod").toURI())));
            System.out.println(txt);
            tokenizer.tokenize("app.rod",txt).forEach(System.out::println);

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
