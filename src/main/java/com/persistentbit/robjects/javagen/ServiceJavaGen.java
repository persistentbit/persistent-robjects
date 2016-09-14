package com.persistentbit.robjects.javagen;


import com.persistentbit.core.collections.PList;
import com.persistentbit.core.sourcegen.SourceGen;
import com.persistentbit.core.tokenizer.Token;
import com.persistentbit.robjects.rod.RodParser;
import com.persistentbit.robjects.rod.RodTokenType;
import com.persistentbit.robjects.rod.RodTokenizer;
import com.persistentbit.robjects.rod.values.*;

import javax.xml.transform.Source;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Created by petermuys on 14/09/16.
 */
public class ServiceJavaGen {

    private final JavaGenOptions options;
    private final RService       service;
    private PList<GeneratedJava>    generatedJava = PList.empty();
    //TODO
    private final String servicePackageName = "com.persistentbit.test";

    private ServiceJavaGen(JavaGenOptions options,RService service) {
        this.options = options;
        this.service = service;
    }

    static public PList<GeneratedJava>  generate(JavaGenOptions options,RService service){
        return new ServiceJavaGen(options,service).generateService();
    }

    public PList<GeneratedJava> generateService(){
        PList<GeneratedJava> result = PList.empty();
        result = result.plusAll(service.enums.map(e -> generateEnum(e)));
        result = result.plusAll(service.valueClasses.map(vc -> generateValueClass(vc)));
        result = result.plusAll(service.remoteClasses.map(rc -> generateRemoteClass(rc)));
        return result.filterNulls().plist();
    }

    public GeneratedJava    generateEnum(REnum e ){
        SourceGen sg = new SourceGen();
        sg.println("// GENERATED CODE: DO NOT CHANGE!");
        sg.println("");
        sg.println("package " + servicePackageName + ";");
        sg.println("");
        sg.bs("public enum " + e.name);{
            sg.println(e.values.toString(","));
        }sg.be();
        return new GeneratedJava(servicePackageName,e.name,sg.writeToString());
    }

    public GeneratedJava    generateValueClass(RValueClass vc){
        SourceGen sg = new SourceGen();
        sg.println("// GENERATED CODE: DO NOT CHANGE!");
        sg.println("");
        sg.println("package " + servicePackageName + ";");
        sg.println("");
        sg.bs("public class " + toString(vc.typeSig));{
            vc.properties.forEach(p -> {
                sg.println(toString(p.valueType) + " " + p.name + ";");
            });
            sg.println("");
            sg.bs("public " + vc.typeSig.name + "(" +
                    vc.properties.map(p -> toString(p.valueType.typeSig) + " " + p.name ).toString(", ")
                    +")");{
                vc.properties.forEach(p -> {
                    String fromValue = p.name;
                    if(p.valueType.required){
                        fromValue = "Objects.requireNonNull(" + p.name + ",\"" + p.name  + " in " + vc.typeSig.name + " can\'t be null\")";
                    }
                    sg.println("this." + p.name + " = " + fromValue + ";");
                });
            }sg.be();
            PList<RProperty> l = vc.properties;
            PList<String> nullValues = PList.empty();
            while(l.lastOpt().isPresent() && l.lastOpt().get().valueType.required == false){
                l = l.dropLast();
                nullValues = nullValues.plus("null");
                sg.bs("public " + vc.typeSig.name + "(" +
                        l.map(p -> toString(p.valueType.typeSig) + " " + p.name ).toString(", ")
                        +")");{
                            sg.println("this(" + l.map(p -> p.name).plusAll(nullValues).toString(",") + ");");
                }sg.be();

            }
        }sg.be();
        return new GeneratedJava(servicePackageName,vc.typeSig.name,sg.writeToString());
    }
    private String toString(RTypeSig sig){
        String gen = sig.generics.isEmpty() ? "" : sig.generics.map(g -> toString(g)).toString("<",",",">");
        String name = sig.name;
        switch(name){
            case "Array": name = "PList"; break;
        }
        return name + gen;
    }

    private String toString(RValueType vt){
        String res = "";
        if(vt.required == false){
            res += "@Nullable ";
        }
        String access = options.generateGetters ? "private" : "public";

        return res + access + " final " + toString(vt.typeSig);

    }

    public GeneratedJava    generateRemoteClass(RRemoteClass rc){
        return null;
    }


    static public void main(String...args) throws Exception{
        String rodFileName= "com.persistentbit.robjects_rodparser_1.0.0.rod";
        URL url = ServiceJavaGen.class.getResource("/" + rodFileName);
        System.out.println("URL: " + url);
        Path path = Paths.get(url.toURI());
        System.out.println("Path  = " + path);
        String rod = new String(Files.readAllBytes(path));
        RodTokenizer tokenizer = new RodTokenizer();
        PList<Token<RodTokenType>> tokens = tokenizer.tokenize(rodFileName,rod);
        RodParser parser = new RodParser("com.persistentbit.test",tokens);
        RService service = parser.parseService();
        System.out.println(service);
        PList<GeneratedJava> gen = ServiceJavaGen.generate(new JavaGenOptions(false),service);
        gen.forEach(gj -> {
            System.out.println(gj.code);
            System.out.println("-----------------------------------");
        });
    }
}
