package com.persistentbit.substema.javagen;


import com.persistentbit.core.Nullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.sourcegen.SourceGen;
import com.persistentbit.core.tokenizer.Token;
import com.persistentbit.substema.annotations.Remotable;
import com.persistentbit.substema.annotations.RemoteCache;
import com.persistentbit.substema.rod.RServiceValidator;
import com.persistentbit.substema.rod.RodParser;
import com.persistentbit.substema.rod.RodTokenType;
import com.persistentbit.substema.rod.RodTokenizer;
import com.persistentbit.substema.rod.values.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by petermuys on 14/09/16.
 */
public class ServiceJavaGen {

    private final JavaGenOptions options;
    private final RService       service;
    private PList<GeneratedJava>    generatedJava = PList.empty();

    private final String servicePackageName;

    private ServiceJavaGen(JavaGenOptions options,String packageName,RService service) {
        this.servicePackageName = packageName;
        this.options = options;
        this.service = service;
    }

    static public PList<GeneratedJava>  generate(JavaGenOptions options,String packageName,RService service){
        return new ServiceJavaGen(options,packageName,service).generateService();
    }

    public PList<GeneratedJava> generateService(){
        RServiceValidator.validate(service);
        PList<GeneratedJava> result = PList.empty();
        result = result.plusAll(service.enums.map(e -> new Generator().generateEnum(e)));
        result = result.plusAll(service.valueClasses.map(vc -> new Generator().generateValueClass(vc)));
        result = result.plusAll(service.remoteClasses.map(rc -> new Generator().generateRemoteClass(rc)));
        return result.filterNulls().plist();
    }

    private class Generator extends SourceGen{
        private PSet<RClass>    imports = PSet.empty();
        private SourceGen       header = new SourceGen();
        private String          packageName;

        public Generator() {
            header.println("// GENERATED CODE: DO NOT CHANGE!");
            header.println("");
        }

        public GeneratedJava    toGenJava(RClass cls){
            SourceGen sg = new SourceGen();
            header.println("package " + servicePackageName + ";");
            header.println("");
            sg.add(header);
            imports.filter(i -> i.packageName.equals(servicePackageName) == false).forEach(i -> {
                sg.println("import " + i.packageName + "." + i.className + ";");
            });
            sg.println("");
            sg.add(this);
            return new GeneratedJava(cls,sg.writeToString());
        }

        public GeneratedJava    generateEnum(REnum e ){
            bs("public enum " + e.name.className);{
                println(e.values.toString(","));
            }be();
            return toGenJava(e.name);
        }
        private void addImport(RClass cls){
            imports = imports.plus(cls);
        }
        private void addImport(Class<?> cls){
            addImport(new RClass(cls.getPackage().getName(),cls.getSimpleName()));
        }

        public GeneratedJava    generateValueClass(RValueClass vc){
            bs("public class " + toString(vc.typeSig));{
                vc.properties.forEach(p -> {

                    println(toString(p.valueType) + " " + p.name + ";");
                });
                println("");
                //***** MAIN CONSTRUCTOR
                bs("public " + vc.typeSig.name.className + "(" +
                        vc.properties.map(p -> toString(p.valueType.typeSig,p.valueType.required) + " " + p.name ).toString(", ")
                        +")");{
                    vc.properties.forEach(p -> {
                        String fromValue = p.name;
                        if(p.valueType.required){
                            addImport(Objects.class);
                            if(isPrimitive(p.valueType.typeSig) == false){
                                fromValue = "Objects.requireNonNull(" + p.name + ",\"" + p.name  + " in " + vc.typeSig.name.className + " can\'t be null\")";
                            }

                        }
                        else {
                            if(options.generateGetters == false) {
                                fromValue = "Optional.ofNullable(" + fromValue + ")";
                            }
                        }
                        println("this." + p.name + " = " + fromValue + ";");
                    });
                }be();
                //****** EXTRA CONSTRUCTORS FOR NULLABLE PROPERTIES
                PList<RProperty> l = vc.properties;
                PList<String> nullValues = PList.empty();
                while(l.lastOpt().isPresent() && l.lastOpt().get().valueType.required == false){
                    l = l.dropLast();
                    nullValues = nullValues.plus("null");
                    bs("public " + vc.typeSig.name.className + "(" +
                            l.map(p -> toString(p.valueType.typeSig,p.valueType.required) + " " + p.name ).toString(", ")
                            +")");{
                        println("this(" + l.map(p -> p.name).plusAll(nullValues).toString(",") + ");");
                    }be();

                }
                //****** GETTERS AND UPDATERS
                vc.properties.forEach(p -> {
                    if(options.generateGetters){
                        String rt = toString(p.valueType.typeSig,p.valueType.required);
                        String vn = p.name;
                        if(p.valueType.required == false){
                            addImport(Optional.class);
                            rt ="Optional<" + rt + ">";
                            vn = "Optional.ofNullable(" + vn + ")";
                        }
                        println("public " + rt + " get" +firstUpper(p.name) + "() { return " + vn + "; }");
                    }
                    if(options.generateUpdaters){
                        String s = "public " + toString(vc.typeSig) + " with" + firstUpper(p.name) + "("+ toString(p.valueType.typeSig,p.valueType.required) + " " + p.name +") { return new ";
                        s += vc.typeSig.name.className;
                        if(vc.typeSig.generics.isEmpty() == false){
                            s += "<>";
                        }
                        s+= "(" + vc.properties.map(param -> {
                            return (param.name.equals(p.name) ? "" : "this.") + param.name;
                        }).toString(", ") + ")";
                        s+= "; }";
                        println(s);
                    }
                    if(options.generateGetters || options.generateUpdaters) {
                        println("");
                    }
                });
                //******* EQUALS
                println("@Override");
                bs("public boolean equals(Object o)");{
                    println("if (this == o) return true;");
                    println("if (o == null || getClass() != o.getClass()) return false;");
                    println("");
                    if(vc.properties.isEmpty() == false) {
                        println(vc.typeSig.name.className + " that = (" + vc.typeSig.name.className + ")o;");
                        println("");
                    }
                    vc.properties.forEach(p -> {
                        String thisVal = p.name;
                        String thatVal = "that." + thisVal;
                        if(p.valueType.required){
                            boolean isPrim = isPrimitive(p.valueType.typeSig);
                            if(isPrim){
                                if(p.valueType.typeSig.name.equals("float")){
                                    println("if(Float.compare(" + thisVal + "," + thatVal + " != 0) return false;");
                                } else if(p.valueType.typeSig.name.equals("double")){
                                    println("if(Double.compare(" + thisVal + "," + thatVal + " != 0) return false;");
                                } else {
                                    println("if(" + thisVal + " != " + thatVal + ") return false;");
                                }
                            } else {
                                println("if(!" + thisVal + ".equals(" + thatVal + ")) return false;");
                            }
                        } else {
                            println("if(" + thisVal + "!= null ? !" + thisVal + ".equals(" + thatVal + ") : " + thatVal + " != null) return false;");
                        }
                    });
                    println("return true;");
                }be();
                //******* HASHCODE
                println("@Override");
                bs("public int hashCode()");{
                    if(vc.properties.isEmpty()){
                        println("return 0;");
                    } else {
                        println("int result;");
                        vc.properties.headMiddleEnd().forEach(t -> {
                            if(t._1 == PStream.HeadMiddleEnd.head || t._1 == PStream.HeadMiddleEnd.headAndEnd){
                                print("result = ");
                            } else {
                                print("result = 31 * result + ");
                            }
                            String value = t._2.name;
                            String hash = value + ".hashCode()";

                            if(t._2.valueType.required) {
                                switch (t._2.valueType.typeSig.name.className) {
                                    case "Float":
                                        hash = "Float.hashCode(" + value + ")";
                                        break;
                                    case "Long":
                                        hash = "Long.hashCode(" + value + ")";
                                        break;
                                    case "Double":
                                        hash = "Double.hashCode(" + value + ")";
                                        break;
                                    case "Short":
                                        hash = "Short.hashCode(" + value + ")";
                                        break;
                                    case "Byte":
                                        hash = "Byte.hashCode(" + value + ")";
                                        break;
                                    case "Boolean":
                                        hash = "Boolean.hashCode(" + value + ")";
                                        break;
                                    case "Integer":
                                        hash = "Integer.hashCode(" + value + ")";
                                        break;

                                }

                            } else {
                                hash = "(" + value + " != null ? " + hash + ": 0)";
                            }
                            println(hash + ";");
                        });
                        println("return result;");
                    }

                }be();
            }be();
            return toGenJava(vc.typeSig.name);
        }
        private String toString(RTypeSig sig){
            return toString(sig,false);
        }
        private String toPrimString(RTypeSig sig){
            return toString(sig,true);
        }


        private String toString(RTypeSig sig,boolean asPrimitive){
            String gen = sig.generics.isEmpty() ? "" : sig.generics.map(g -> toString(g)).toString("<",",",">");
            String pname = sig.name.packageName;
            String name = sig.name.className;

            switch(name){
                case "List": name = "PList"; addImport(PList.class); break;
                case "Set": name = "PSet"; addImport(PSet.class); break;
                case "Map": name= "PMap"; addImport(PMap.class); break;

                case "Boolean": name = asPrimitive ? "boolean" : name; break;
                case "Byte": name = asPrimitive ? "byte" : name; break;
                case "Short": name = asPrimitive ? "short" : name; break;
                case "Integer": name = asPrimitive ? "int" : name; break;
                case "Long": name = asPrimitive ? "long" : name; break;
                case "Float": name = asPrimitive ? "float" : name; break;
                case "Double": name = asPrimitive ? "double" : name; break;

                case "String": break;

                default:
                    addImport(new RClass(pname,name));
                    break;
            }

            return name + gen;
        }

        private boolean isPrimitive(RTypeSig sig){
            return toString(sig,true).equals(toString(sig,false)) == false;
        }

        private String firstUpper(String s){
            return s.substring(0,1).toUpperCase() + s.substring(1);
        }

        private String toString(RValueType vt){
            String res = "";
            String value = vt.required ? toPrimString(vt.typeSig) : toString(vt.typeSig);
            if(vt.required == false){
                addImport(Nullable.class);

                if(options.generateGetters == false){
                    addImport(Optional.class);
                    value = "Optional<" + value + ">";
                } else {
                    res += "@Nullable ";
                }
            }
            String access = options.generateGetters ? "private" : "public";

            return res + access + " final " + value;

        }

        public GeneratedJava    generateRemoteClass(RRemoteClass rc){
            addImport(Remotable.class);
            println("@Remotable");
            bs("public interface " + rc.name.className); {
                rc.functions.forEach(f -> {
                    String retType;
                    addImport(CompletableFuture.class);
                    if(f.resultType == null) {
                        retType = "Object";
                    } else {
                        retType = toString(f.resultType.typeSig);
                        if(f.resultType.required == false){
                            retType = "Optional<" + retType + ">";
                            addImport(Optional.class);
                        }
                    }
                    if(f.cached){
                        addImport(RemoteCache.class);
                        println("@RemoteCache");
                    }
                    println("CompletableFuture<" + retType + ">\t" + f.name + "(" +
                            f.params.map( p -> toString(p.valueType.typeSig) + " " + p.name).toString(", ") + ");"
                    );
                });

            }be();

            return toGenJava(rc.name);
        }

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
        String packageName  = "com.persistentbit.test";
        RodParser parser = new RodParser(packageName,tokens);
        RService service = parser.parseService();
        System.out.println(service);
        PList<GeneratedJava> gen = ServiceJavaGen.generate(new JavaGenOptions(),packageName,service);
        gen.forEach(gj -> {
            System.out.println(gj.code);
            System.out.println("-----------------------------------");
        });
    }
}
