package com.persistentbit.substema.javagen;


import com.persistentbit.core.Nullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.sourcegen.SourceGen;
import com.persistentbit.core.tokenizer.Token;
import com.persistentbit.core.utils.builders.NOT;
import com.persistentbit.core.utils.builders.SET;
import com.persistentbit.substema.annotations.Remotable;
import com.persistentbit.substema.annotations.RemoteCache;
import com.persistentbit.substema.compiler.*;
import com.persistentbit.substema.compiler.values.*;
import com.persistentbit.substema.compiler.values.expr.RConst;
import com.persistentbit.substema.compiler.values.expr.RConstString;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Created by petermuys on 14/09/16.
 */
public class ServiceJavaGen {

    private final JavaGenOptions options;
    private final RSubstema service;
    private PList<GeneratedJava>    generatedJava = PList.empty();

    private final String servicePackageName;

    private ServiceJavaGen(JavaGenOptions options,RSubstema service) {
        this.servicePackageName = service.getPackageName();
        this.options = options;
        this.service = service;
    }

    static public PList<GeneratedJava>  generate(JavaGenOptions options,RSubstema service){
        return new ServiceJavaGen(options,service).generateService();
    }

    public PList<GeneratedJava> generateService(){
        //RServiceValidator.validate(service);
        PList<GeneratedJava> result = PList.empty();
        result = result.plusAll(service.getEnums().map(e -> new Generator().generateEnum(e)));
        result = result.plusAll(service.getValueClasses().map(vc -> new Generator().generateValueClass(vc)));
        result = result.plusAll(service.getRemoteClasses().map(rc -> new Generator().generateRemoteClass(rc)));
        result = result.plusAll(service.getInterfaceClasses().map(ic -> new Generator().generateInterfaceClass(ic)));
        return result.filterNulls().plist();
    }

    private class Generator extends SourceGen{
        private PSet<RClass>    imports = PSet.empty();
        private SourceGen       header = new SourceGen();
        private String          packageName;

        public Generator() {
            this.packageName = servicePackageName;
            header.println("// GENERATED CODE: DO NOT CHANGE!");
            header.println("");
        }

        public GeneratedJava    toGenJava(RClass cls){
            SourceGen sg = new SourceGen();
            header.println("package " + servicePackageName + ";");
            header.println("");
            sg.add(header);
            imports.filter(i -> i.getPackageName().equals(servicePackageName) == false).forEach(i -> {
                sg.println("import " + i.getPackageName() + "." + i.getClassName() + ";");
            });
            sg.println("");
            sg.add(this);
            return new GeneratedJava(cls,sg.writeToString());
        }

        public GeneratedJava    generateEnum(REnum e ){
            bs("public enum " + e.getName().getClassName());{
                println(e.getValues().toString(","));
            }be();
            return toGenJava(e.getName());
        }
        private void addImport(RClass cls){
            imports = imports.plus(cls);
        }
        private void addImport(Class<?> cls){
            addImport(new RClass(cls.getPackage().getName(),cls.getSimpleName()));
        }

        public GeneratedJava generateInterfaceClass(RInterfaceClass ic){
            bs("public interface " + ic.getName().getClassName()); {
                //****** GETTERS AND UPDATERS
                ic.getProperties().forEach(p -> {
                    if(options.generateGetters){
                        String rt = toString(p.getValueType().getTypeSig(),p.getValueType().isRequired());
                        String vn = p.getName();
                        if(p.getValueType().isRequired() == false){
                            addImport(Optional.class);
                            rt ="Optional<" + rt + ">";
                            vn = "Optional.ofNullable(" + vn + ")";
                        }
                        println("public " + rt + " get" +firstUpper(p.getName()) + "();");
                    }
                    if(options.generateUpdaters){
                        String s = "public " + ic.getName().getClassName() + " with" + firstUpper(p.getName()) + "("+ toString(p.getValueType().getTypeSig(),p.getValueType().isRequired()) + " " + p.getName() +");";

                        println(s);
                    }
                    if(options.generateGetters || options.generateUpdaters) {
                        println("");
                    }
                });
            }be();
            return toGenJava(ic.getName());
        }

        /**
         * Generate java for a Case Class (or value class)
         * @param vc The RValueClass to generate code for
         * @return  The generated Java code
         */
        public GeneratedJava    generateValueClass(RValueClass vc){
            //Lets first generate the javadoc (if any)
            PList<RAnnotation> docs = getAnnotations(vc.getAnnotations(), SubstemaUtils.docRClass);
            if(docs.isEmpty() == false){
                println("/**");
                docs.forEach(d -> {
                    PMap<String,RConst> props = d.getProperties();
                    RConstString info = (RConstString)props.getOpt("info").orElseGet(() -> props.get(null));
                    println(" * " + info.getValue());
                });
                println(" */");
            }


            String impl = vc.getInterfaceClasses().isEmpty() ? "" :
                    " implements " + vc.getInterfaceClasses().map(ic -> ic.getClassName()).toString(",");

            bs("public class " + toString(vc.getTypeSig())+ impl);{
                vc.getProperties().forEach(p -> {

                    println(toString(p.getValueType(),true) + " " + p.getName() + ";");
                });
                println("");
                //***** MAIN CONSTRUCTOR
                bs("public " + vc.getTypeSig().getName().getClassName() + "(" +
                        vc.getProperties().map(p -> toString(p.getValueType().getTypeSig(),p.getValueType().isRequired() && p.getDefaultValue().isPresent() == false) + " " + p.getName() ).toString(", ")
                        +")");{
                    vc.getProperties().forEach(p -> {
                        String fromValue = p.getName();
                        if(p.getDefaultValue().isPresent()){

                            fromValue = p.getName() + " != null ? " + fromValue + " : " + RConstToJava.toJava(packageName,rcls -> addImport(rcls),p.getDefaultValue().get());
                        } else {
                            if(p.getValueType().isRequired()){
                                addImport(Objects.class);
                                if(isPrimitive(p.getValueType().getTypeSig()) == false){
                                    fromValue = "Objects.requireNonNull(" + p.getName() + ",\"" + p.getName()  + " in " + vc.getTypeSig().getName().getClassName() + " can\'t be null\")";
                                }

                            }
                            else {
                                if(options.generateGetters == false) {
                                    fromValue = "Optional.ofNullable(" + fromValue + ")";
                                }
                            }

                        }
                        println("this." + p.getName() + " = " + fromValue + ";");
                    });
                }be();
                //****** EXTRA CONSTRUCTORS FOR NULLABLE PROPERTIES
                PList<RProperty> req = vc.getProperties().filter(p -> isRequired(p));
                if(req.size() != vc.getProperties().size()) {
                    bs("public " + vc.getTypeSig().getName().getClassName() + "(" +
                            req.map(p -> toString(p.getValueType().getTypeSig(), p.getValueType().isRequired()) + " " + p.getName()).toString(", ")
                            + ")");
                    {
                        println("this(" + vc.getProperties().map(p -> isRequired(p) ? p.getName() : "null").toString(",") + ");");
                    }
                    be();
                }

                //****** GETTERS AND UPDATERS
                vc.getProperties().forEach(p -> {
                    if(options.generateGetters){
                        String rt = toString(p.getValueType().getTypeSig(),p.getValueType().isRequired());
                        String vn = p.getName();
                        if(p.getValueType().isRequired() == false){
                            addImport(Optional.class);
                            rt ="Optional<" + rt + ">";
                            vn = "Optional.ofNullable(" + vn + ")";
                        }
                        println("public " + rt + " get" +firstUpper(p.getName()) + "() { return " + vn + "; }");
                    }
                    if(options.generateUpdaters){
                        String s = "public " + toString(vc.getTypeSig()) + " with" + firstUpper(p.getName()) + "("+ toString(p.getValueType().getTypeSig(),p.getValueType().isRequired()) + " " + p.getName() +") { return new ";
                        s += vc.getTypeSig().getName().getClassName();
                        if(vc.getTypeSig().getGenerics().isEmpty() == false){
                            s += "<>";
                        }
                        s+= "(" + vc.getProperties().map(param -> {
                            return (param.getName().equals(p.getName()) ? "" : "this.") + param.getName();
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
                    if(vc.getProperties().isEmpty() == false) {
                        println(vc.getTypeSig().getName().getClassName() + " that = (" + vc.getTypeSig().getName().getClassName() + ")o;");
                        println("");
                    }
                    vc.getProperties().forEach(p -> {
                        String thisVal = p.getName();
                        String thatVal = "that." + thisVal;
                        if(p.getValueType().isRequired()){
                            boolean isPrim = isPrimitive(p.getValueType().getTypeSig());
                            if(isPrim){
                                if(p.getValueType().getTypeSig().getName().equals("float")){
                                    println("if(Float.compare(" + thisVal + "," + thatVal + " != 0) return false;");
                                } else if(p.getValueType().getTypeSig().getName().equals("double")){
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
                    if(vc.getProperties().isEmpty()){
                        println("return 0;");
                    } else {
                        println("int result;");
                        vc.getProperties().headMiddleEnd().forEach(t -> {
                            if(t._1 == PStream.HeadMiddleEnd.head || t._1 == PStream.HeadMiddleEnd.headAndEnd){
                                print("result = ");
                            } else {
                                print("result = 31 * result + ");
                            }
                            String value = t._2.getName();
                            String hash = value + ".hashCode()";

                            if(t._2.getValueType().isRequired()) {
                                switch (t._2.getValueType().getTypeSig().getName().getClassName()) {
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

                    //******* TOSTRING
                    generateToString(vc);

                    //******* BUILDER

                    bs("static public class Builder" + getBuilderGenerics(vc));{
                        vc.getProperties().forEach(p -> {

                            println(toString(p.getValueType(),false) + " " + p.getName() + ";");

                        });
                        println("");
                        vc.getProperties().forEach(p ->{
                            String gen = getBuilderGenerics(vc,PMap.<String,String>empty().put(p.getName(),"SET"));
                            bs("public Builder" + gen + " set" + firstUpper(p.getName()) + "("+ toString(p.getValueType().getTypeSig(),p.getValueType().isRequired()) + " " + p.getName() + ")");{
                                println("this." + p.getName() + " = " + p.getName() + ";");
                                println("return (Builder" + getBuilderGenerics(vc,PMap.<String,String>empty().put(p.getName(),"SET")) + ") this;");
                            }be();
                        });





                    }be();
                String onlyGen = vc.getTypeSig().getGenerics().map(g -> g.getName().getClassName()).toString(",");
                onlyGen = onlyGen.isEmpty() ? "" : "<" + onlyGen + ">";
                String not = getRequiredProps(vc).map(v -> "NOT").plusAll(vc.getTypeSig().getGenerics().map(g -> g.getName().getClassName())).toString(",");
                String set = getRequiredProps(vc).map(v -> "SET").plusAll(vc.getTypeSig().getGenerics().map(g -> g.getName().getClassName())).toString(",");
                not = not.isEmpty() ? not : "<" + not + ">";
                set = set.isEmpty() ? set : "<" + set + ">";
                addImport(Function.class);
                String p = "Function<Builder" + not + ",Builder" + set + "> supplier";
                bs("static public " + onlyGen + " " + toString(vc.getTypeSig()) + " build(" + p + ")");{

                    println("Builder b = supplier.apply(new Builder"+(getBuilderGenerics(vc).isEmpty() ? "" : "<>") +  "());");
                    println("return new "+ vc.getTypeSig().getName().getClassName() + "(" + vc.getProperties().map(v -> "b." + v.getName()).toString(", ") + ");");
                    be();}
            }be();
            return toGenJava(vc.getTypeSig().getName());
        }

        /**
         * Generate a toString() java method for a RValueClass
         * @param vc The case class
         *
         */
        public void generateToString(RValueClass vc){
            println("@Override");
            bs("public String toString()");{
                println("return \"" + vc.getTypeSig().getName().getClassName() + "<<\" +");
                indent();
                boolean first = true;

                for(RProperty p : vc.getProperties()){
                    String res = "\", ";
                    if(first){
                        res = "\"";
                        first = false;
                    }
                    res += p.getName() + "=\"" + " + " + p.getName();

                    if(p.getValueType().isRequired() == false){
                        res = "(" + p.getName() + " == null ? \"\" : " + res + ")";
                    }
                    println(res + " +");
                }
                println("\">>\";");
                outdent();
            }be();
            println("");
        }

        private PList<RProperty>   getRequiredProps(RValueClass vc) {
            return vc.getProperties().filter(p->p.getDefaultValue().isPresent() == false && p.getValueType().isRequired());
        }

        private String getBuilderGenerics(RValueClass vc){
            return getBuilderGenerics(vc,PMap.empty());
        }
        private String getBuilderGenerics(RValueClass vc, PMap<String,String> namesReplace){
            PList<String> requiredProperties = getRequiredProps(vc).zipWithIndex().map(t -> namesReplace.getOpt(t._2.getName()).orElse("_T" + (t._1+1))).plist();
            if(requiredProperties.isEmpty()==false){
                addImport(SET.class);
                addImport(NOT.class);
            }
            requiredProperties = requiredProperties.plusAll(vc.getTypeSig().getGenerics().map(g -> g.getName().getClassName()));
            if(requiredProperties.isEmpty()){
                return "";
            }
            return requiredProperties.toString("<",",",">");
        }

        private boolean isRequired(RProperty p){
            return p.getDefaultValue().isPresent() == false && p.getValueType().isRequired();
        }

        private String toString(RTypeSig sig){
            return toString(sig,false);
        }
        private String toPrimString(RTypeSig sig){
            return toString(sig,true);
        }


        private String toString(RTypeSig sig,boolean asPrimitive){
            String gen = sig.getGenerics().isEmpty() ? "" : sig.getGenerics().map(g -> toString(g)).toString("<",",",">");
            String pname = sig.getName().getPackageName();
            String name = sig.getName().getClassName();

            switch(name){
                case "List": name = "PList"; addImport(PList.class); break;
                case "Set": name = "PSet"; addImport(PSet.class); break;
                case "Map": name= "PMap"; addImport(PMap.class); break;
                case "Date": name = "LocalDate"; addImport(LocalDate.class);break;
                case "DateTime": name= "LocalDateTime"; addImport(LocalDateTime.class);break;

                case "Boolean": name = asPrimitive ? "boolean" : name; break;
                case "Byte": name = asPrimitive ? "byte" : name; break;
                case "Short": name = asPrimitive ? "short" : name; break;
                case "Integer": name = asPrimitive ? "int" : name; break;
                case "Long": name = asPrimitive ? "long" : name; break;
                case "Float": name = asPrimitive ? "float" : name; break;
                case "Double": name = asPrimitive ? "double" : name; break;

                case "String": break;

                default:
                    if(pname.isEmpty()){
                        throw new SubstemaException("Don't know interal class " + name);
                    }
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

        private String toString(RValueType vt,boolean isFinal){
            String res = "";
            String value = vt.isRequired() ? toPrimString(vt.getTypeSig()) : toString(vt.getTypeSig());
            if(vt.isRequired() == false){
                addImport(Nullable.class);

                if(options.generateGetters == false){
                    addImport(Optional.class);
                    value = "Optional<" + value + ">";
                } else {
                    res += "@Nullable ";
                }
            }
            String access =  options.generateGetters ? "private" : "public";
            access += isFinal ?  " final " : " ";
            return res + access  + value;

        }

        public GeneratedJava    generateRemoteClass(RRemoteClass rc){
            addImport(Remotable.class);
            println("@Remotable");
            bs("public interface " + rc.getName().getClassName()); {
                rc.getFunctions().forEach(f -> {
                    String retType;
                    addImport(CompletableFuture.class);
                    if(f.getResultType().isPresent() == false) {
                        retType = "Object";
                    } else {
                        retType = toString(f.getResultType().get().getTypeSig());
                        if(f.getResultType().get().isRequired() == false){
                            retType = "Optional<" + retType + ">";
                            addImport(Optional.class);
                        }
                    }
                    if(f.isCached()){
                        addImport(RemoteCache.class);
                        println("@RemoteCache");
                    }
                    println("CompletableFuture<" + retType + ">\t" + f.getName() + "(" +
                            f.getParams().map( p -> toString(p.getValueType().getTypeSig()) + " " + p.getName()).toString(", ") + ");"
                    );
                });

            }be();

            return toGenJava(rc.getName());
        }

    }

    /**
     * Filter an annotationList using the annotation RClass name
     * @param al The list with annotations
     * @param anCls The RClass to filter
     * @return The list with annotations with the same RClass
     */
    public PList<RAnnotation> getAnnotations(PList<RAnnotation> al, RClass anCls){
        return al.filter(an -> an.getName().equals(anCls));
    }



    static public void main(String...args) throws Exception{
        String rodFileName= "com.persistentbit.parser.substema";
        URL url = ServiceJavaGen.class.getResource("/" + rodFileName);
        System.out.println("URL: " + url);
        Path path = Paths.get(url.toURI());
        System.out.println("Path  = " + path);
        String rod = new String(Files.readAllBytes(path));
        SubstemaTokenizer tokenizer = new SubstemaTokenizer();
        PList<Token<SubstemaTokenType>> tokens = tokenizer.tokenize(rodFileName,rod);
        String packageName  = "com.persistentbit.test";
        SubstemaParser parser = new SubstemaParser(packageName,tokens);
        RSubstema service = parser.parseSubstema();
        System.out.println(service);
        PList<GeneratedJava> gen = ServiceJavaGen.generate(new JavaGenOptions(),service);
        gen.forEach(gj -> {
            System.out.println(gj.code);
            System.out.println("-----------------------------------");
        });
    }
}
