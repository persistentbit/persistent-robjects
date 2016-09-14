package com.persistentbit.robjects.rod;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.tokenizer.Token;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.robjects.describe.RemoteServiceDescription;

import java.util.function.Supplier;

import static com.persistentbit.robjects.rod.RodTokenType.*;
/**
 * Created by petermuys on 12/09/16.
 */
public class RodParser {
    private PStream<Token<RodTokenType>> tokens;
    private Token<RodTokenType> current;
    private String packageName;




    static private class RTypeSig extends BaseValueClass{
        public final String             name;
        public final PList<RTypeSig>    generics;

        public RTypeSig(String name, PList<RTypeSig> generics) {
            this.name = name;
            this.generics = generics;
        }
    }

    static private class RProperty extends BaseValueClass{
        public final String     name;
        public final RTypeSig   typeSig;
        public final boolean    required;

        public RProperty(String name, RTypeSig typeSig,boolean required) {
            this.name = name;
            this.typeSig = typeSig;
            this.required = required;
        }
    }

    static private class RValueClass extends BaseValueClass{
        public final RTypeSig           typeSig;
        public final PList<RProperty>   properties;

        public RValueClass(RTypeSig typeSig, PList<RProperty> properties) {
            this.typeSig = typeSig;
            this.properties = properties;
        }
    }


    public RodParser(String packageName,PStream<Token<RodTokenType>> tokens){
        this.packageName = packageName;
        this.tokens = tokens.filter(t -> t.type != tComment);
        next();
    }
    private Token<RodTokenType> next() {
        if(tokens.isEmpty()){
            if(current.type == RodTokenType.tEOF) {
                throw new RodParserException(current.pos, "Unexpected End-Of-File");
            }
            current = new Token<>(current.pos,tEOF,"");
            return current;
        }
        current = tokens.head();
        tokens = tokens.tail();
        return current;
    }

    public RemoteServiceDescription    parseService() {
        if(current.type == tPackage){
            packageName = parsePackage();
        }
        PList<RValueClass>  values = PList.empty();
        while(current.type != RodTokenType.tEOF){
            switch(current.type){
                //case tImport: handleImport();
                case tValue: values = values.plus(parseValueClass());
                case tRemote: handleRemote();
            }
        }
        return null;
    }

    private RTypeSig parseTypeSignature() {
        assertType(tIdentifier,"Class name expected.");
        String className = current.text;

        next(); //skip class name
        PList<RTypeSig> generics = PList.empty();
        if(current.type == tGenStart){
            next(); //skip <
            while(current.type != tEOF){
                generics = generics.plus(parseTypeSignature());
                if(current.type == tGenEnd){
                    next(); //skip >
                    break;
                }
                skip(tComma,"Expected ',' ");
            }
        }
        return new RTypeSig(className,generics);
    }
    private void assertType(RodTokenType type, String msg){
        if(current.type != type){
            throw new RodParserException(current.pos,msg);
        }
    }

    private void skip(RodTokenType type,String msg){
        assertType(type,msg);
        next();
    }


    private RValueClass parseValueClass(){

        skip(tValue,"'value' expected");
        skip(tClass,"'class' expected");
        RTypeSig sig = parseTypeSignature();
        PList<RProperty> props = PList.empty();
        if(current.type == tBlockStart){
            next();
            while(current.type!= tEOF && current.type != tBlockEnd){
                props = props.plus(parseRProperty());
            }
            skip(tBlockEnd,"'}' expected");
        }
        return new RValueClass(sig,props);
    }

    private RProperty   parseRProperty() {
        assertType(tIdentifier,"property name expected");
        String name = current.text;
        next(); //skip name;
        boolean required = true;
        if(current.type == tQuestion){
            required = false;
            next();
        }
        skip(tColon,"':' expected after property name");
        RTypeSig sig = parseTypeSignature();
        skipEndOfStatement();
        return new RProperty(name,sig,required);
    }

    private void handleRemote() {

    }

    private String parsePackage() {
        skip(tPackage,"package expected");
        String res = sep(tComma,() -> {
            assertType(tIdentifier,"name expected");
            String name = current.text;
            next();
            return name;
        }).toString(".");
        skipEndOfStatement();
        return res;
    }

    private void skipEndOfStatement() {
        skip(tSemiColon,"';' expected after statement.");
    }



    private <T> PList<T> sep(RodTokenType sep,Supplier<T> r){
        PList<T> res = PList.empty();
        while(current.type != tEOF){
            res = res.plus(r.get());
            if(current.type != sep){
                return res;
            }
            next();
        }
        return res;
    }

    private void handleImport() {

    }

    static public void main(String...args) throws Exception{
        String test = PList.val(
                "package be.schaubroeck;",
                "value class App{",
                "}"
                ).toString("\n");
        System.out.println(test);
        PList<Token<RodTokenType>> tokens = new RodTokenizer().tokenize("test.rod",test);
        tokens.forEach(System.out::println);
        new RodParser("com.undefined",tokens).parseService();
    }
}
