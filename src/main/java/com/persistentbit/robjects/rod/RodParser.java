package com.persistentbit.robjects.rod;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.tokenizer.SimpleTokenizer;
import com.persistentbit.core.tokenizer.Token;
import com.persistentbit.jjson.mapping.description.JJClass;
import com.persistentbit.jjson.mapping.description.JJTypeSignature;
import com.persistentbit.robjects.RemoteService;
import com.persistentbit.robjects.describe.RemoteServiceDescription;
import static com.persistentbit.robjects.rod.RodTokenType.*;
/**
 * Created by petermuys on 12/09/16.
 */
public class RodParser {
    private PStream<Token<RodTokenType>> tokens;
    private Token<RodTokenType> current;
    private String packageName;

    public RodParser(String packageName,PStream<Token<RodTokenType>> tokens){
        this.packageName = packageName;
        this.tokens = tokens.filter(t -> t.type == tComment);
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
        while(current.type != RodTokenType.tEOF){
            switch(current.type){
                case tPackage: handlePackage();
                case tImport: handleImport();
                case tValue: handleValue();
                case tRemote: handleRemote();
            }
        }
        return null;
    }

    private JJTypeSignature parseTypeSignature() {
        String className = current.text;
        next(); //skip class name
        PMap<String,JJTypeSignature> generics = PMap.empty();
        if(current.type == tGenStart){
            next();//skip <
            int count = 1;
            do{
                JJTypeSignature genType = parseTypeSignature();
                generics = generics.put("??" + count,genType);
                if(current.type == tComma){
                    next(); //skip ,
                } else if(current.type == tGenEnd){
                    next(); //skip >
                    break;
                }
                count++;
            }while(true);
        }
        JJTypeSignature.JsonType type = JJTypeSignature.JsonType.jsonObject;
        switch (className){
            case "Array": type= JJTypeSignature.JsonType.jsonArray;break;
            case "String": type= JJTypeSignature.JsonType.jsonString; break;
            case "Int": type= JJTypeSignature.JsonType.jsonNumber; break;
            case "Double": type= JJTypeSignature.JsonType.jsonNumber; break;
            case "Float": type= JJTypeSignature.JsonType.jsonNumber; break;
            case "Short":type= JJTypeSignature.JsonType.jsonNumber; break;
            case "Long":type= JJTypeSignature.JsonType.jsonNumber; break;
            case "Boolean": type= JJTypeSignature.JsonType.jsonBoolean; break;
            case "Byte": type= JJTypeSignature.JsonType.jsonNumber; break;
            case "Set": type= JJTypeSignature.JsonType.jsonSet; break;
            case "Map": type= JJTypeSignature.JsonType.jsonMap; break;
        }
        return new JJTypeSignature(new JJClass(packageName,className),type,generics);
    }

    private void handleValue(){

    }

    private void handleRemote() {

    }

    private void handlePackage() {

    }
    private void handleImport() {

    }


}
