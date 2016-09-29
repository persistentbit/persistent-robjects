package com.persistentbit.substema.compiler;

import com.persistentbit.core.tokenizer.Pos;

/**
 * Created by petermuys on 13/09/16.
 */
public class SubstemaParserException extends RuntimeException{
    private final Pos pos;

    public SubstemaParserException(Pos pos, String message) {
        super(asMessage(pos,message));
        this.pos = pos;
    }
    private static String asMessage(Pos pos,String msg){
        return "Parser error in " +  pos + ": "+ msg;
    }

    public SubstemaParserException(Pos pos, String message, Throwable cause) {
        super(asMessage(pos,message),cause);
        this.pos = pos;
    }

    public SubstemaParserException(Pos pos, Throwable cause) {
        this(pos,asMessage(pos,cause.getMessage()),cause);
    }

    public Pos getPos() {
        return pos;
    }
}
