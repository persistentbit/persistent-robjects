package com.persistentbit.substema.compiler;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.POrderedMap;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.tokenizer.Pos;
import com.persistentbit.core.tokenizer.Token;
import com.persistentbit.core.utils.StringUtils;
import com.persistentbit.core.function.Function2;
import com.persistentbit.substema.compiler.values.*;
import com.persistentbit.substema.compiler.values.expr.*;

import java.util.function.Supplier;

import static com.persistentbit.substema.compiler.SubstemaTokenType.*;

/**
 * Created by petermuys on 12/09/16.
 */
public class SubstemaParser {
    private PStream<Token<SubstemaTokenType>> tokens;
    private Token<SubstemaTokenType> current;
    private String packageName;

    public SubstemaParser(String packageName, PStream<Token<SubstemaTokenType>> tokens){
        this.packageName = packageName;
        this.tokens = tokens;
        if(tokens.isEmpty()){
            current = new Token<>(new Pos(packageName,1,1),tEOF,"");
        } else {
            next();
        }
    }
    private Token<SubstemaTokenType> next() {
        if(tokens.isEmpty()){
            if(current.type == SubstemaTokenType.tEOF) {
                throw new SubstemaParserException(current.pos, "Unexpected End-Of-File");
            }
            current = new Token<>(current.pos,tEOF,"");
            return current;
        }
        current = tokens.head();
        tokens = tokens.tail();
        return current;
    }

    /**
     * Peek at the next token value, without changing the current token.<br>
     * @return The next token or tEOF if there are no more tokens.
     */
    private Token<SubstemaTokenType> peek(){
         return tokens.headOpt().orElseGet(() -> new Token<>(current.pos,tEOF,""));
    }


    /**
     * Main function to parse a Substema File.<br>
     *
     * @return The parsed RSubstema.
     */
    public RSubstema parseSubstema() {
        RPackage packageDef =   null;
        PList<RImport> imports = PList.empty();
        PList<RValueClass>  values = PList.empty();
        PList<RRemoteClass> remotes = PList.empty();
        PList<REnum> enums = PList.empty();
        PList<RInterfaceClass> interfaces = PList.empty();
        PList<RAnnotationDef> annotationDefs = PList.empty();
        while(current.type != SubstemaTokenType.tEOF){
            PList<RAnnotation> annotations = parseAnnotations();
            switch(current.type){
                case tPackage:
                    if(packageDef != null){
                        throw new SubstemaParserException(current.pos,"There can be only one package definition");
                    }
                    packageDef = new RPackage(annotations);
                    next();//skip package
                    skipEndOfStatement();
                    break;
                case tImport:
                    if(annotations.isEmpty() == false){
                        throw new SubstemaParserException(current.pos,"Did not expect annotations for an import statement");
                    }
                    imports = imports.plus(parseImport());break;
                case tCase: values = values.plus(parseValueClass(annotations));
                    break;
                case tRemote: remotes = remotes.plus(parseRemoteClass(annotations));
                    break;
                case tEnum: enums = enums.plus(parseEnum(annotations));
                    break;
                case tInterface: interfaces = interfaces.plus(parseInterface(annotations));
                    break;
                case tAnnotation:
                    if(annotations.isEmpty() == false){
                        throw new SubstemaParserException(current.pos,"Dit not expect annotations for an annotation definition");
                    }
                    annotationDefs = annotationDefs.plus(parseAnnotionDef());break;
                default:
                    throw new SubstemaParserException(current.pos,"Expected a definition, not '" + current.text + "'");
            }
        }
        if(packageDef == null) {
            packageDef = new RPackage(PList.empty());
        }
        RSubstema service = new RSubstema(packageDef,imports,packageName,enums,values,remotes,interfaces,annotationDefs);

        return service;
    }

    /**
     * Parse an annotation definition.<br>
     * Example: <br>
     * {@code<code>
     *     annotation AnnotationName{
     *         property1:Type = default1;
     *         property2:Type;
     *         property3:?Type;
     *     }
     *
     * </code>
     * }
     *
     *
     * @return
     */
    private RAnnotationDef parseAnnotionDef(){
        skip(tAnnotation,"'annotation' keyword expected");
        RClass cls = parseRClass(packageName);
        PList<RProperty> props = PList.empty();
        if(current.type == tBlockStart){
            next();
            while(current.type!= tEOF && current.type != tBlockEnd){
                props = props.plus(parseRProperty(parseAnnotations()));
            }
            skip(tBlockEnd,"'}' expected for the end of the annotation definition of " + cls.getClassName());
        }
        return new RAnnotationDef(cls,props);
     }

    /**
     * Try to parse a list of annotations values at the current position.<br>
     * If none are found, then an empty PList is returned.<br>
     * if a doc token <<....>> is found than this is transformed to a @Doc annotation<br>
     * @return The List of parsed RAnnotations
     */
    private PList<RAnnotation> parseAnnotations(){
        PList<RAnnotation> result = PList.empty();
        while(current.type == tAt || current.type == tDoc){
            if(current.type == tDoc){
                result = result.plus(
                        new RAnnotation(
                                SubstemaUtils.docRClass,
                                PMap.<String,RConst>empty().put(
                                        "info",
                                        new RConstString(
                                                StringUtils.escapeToJavaString(
                                                        current.text.substring(2,current.text.length()-2)
                                                )

                                        )
                                )
                        )
                );
                next();//Skip doc token
            } else {
                next();//skip @
                RClass name = parseRClass("");
                PMap<String,RConst> values = PMap.empty();

                if(current.type == tOpen){
                    next();
                    if(current.type != tClose){
                        values = values.plusAll(sep(tComma, () -> {
                            String propName = null;

                            if(peek().type == tAssign){
                                propName = current.text;
                                skip(tIdentifier,"propery name expected for annotation " + name);

                            } //else{
                            //if(isFirstDefault == false){
                            //    throw new SubstemaParserException(current.pos,"There can be maximum 1 default value");
                            //}
                            //isFirstDefault = false;
                            //}

                            RConst value = parseConst();
                            return Tuple2.of(propName,value);
                        }));
                    }

                    skip(tClose,"')' expected to close the annotation " + name);
                }
                result = result.plus(new RAnnotation(name,values));
            }


        }
        return result;
    }



    /**
     * Parse an Import statement
     */
    private RImport parseImport() {
        skip(tImport,"'import' expected.");
        String packageName = parsePackageName();
        skip(tSemiColon,"';' expected after import package name");
        return new RImport(packageName);
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
        return new RTypeSig(new RClass("",className),generics);
    }
    private void assertType(SubstemaTokenType type, String msg){
        if(current.type != type){
            throw new SubstemaParserException(current.pos,msg);
        }
    }

    private void skip(SubstemaTokenType type, String msg){
        assertType(type,msg);
        next();
    }

    /**
     * parse the Class name and create a RClass with the given packageName and the parsed class Name
     * @param packageName The packageName for the RClass
     * @return a new RClass
     */
    private RClass parseRClass(String packageName){
        String name = current.text;
        skip(tIdentifier,"identifier expected!");
        return new RClass(packageName,name);
    }

    private RInterfaceClass parseInterface(PList<RAnnotation> annotations) {
        skip(tInterface,"'interface' expected");
        RClass name =  parseRClass(packageName);
        PList<RProperty> p = PList.empty();
        if(current.type == tBlockStart){
            next();
            while(current.type!= tEOF && current.type != tBlockEnd){
                p = p.plus(parseRProperty(parseAnnotations()));
            }
            skip(tBlockEnd,"'}' expected");
        }
        return new RInterfaceClass(name,p,annotations);
    }

    /**
     * Parse a case class definition.
     * @param annotations
     * @return
     */
    private RValueClass parseValueClass(PList<RAnnotation> annotations){

        skip(tCase,"'value' expected");
        skip(tClass,"'class' expected");
        RTypeSig sig = parseTypeSignature();
        PList<RClass> interfaces = PList.empty();
        if(current.type == tImplements){
            next();//skip implements;
            interfaces = sep(tComma,()-> parseRClass(""));
        }


        PList<RProperty> props = PList.empty();
        if(current.type == tBlockStart){
            next();
            while(current.type!= tEOF && current.type != tBlockEnd){
                props = props.plus(parseRProperty(parseAnnotations()));
            }
            skip(tBlockEnd,"'}' expected");
        }
        return new RValueClass(sig,props,interfaces,annotations);
    }

    private RValueType  parseRValueType() {
        boolean required = true;
        if(current.type == tQuestion){
            required = false;
            next();
        }
        RTypeSig sig = parseTypeSignature();
        return new RValueType(sig,required);
    }

    private RProperty   parseRProperty(PList<RAnnotation> annotations) {
        assertType(tIdentifier,"property name expected");
        String name = current.text;
        next(); //skip name;

        skip(tColon,"':' expected after property name");
        RValueType valueType = parseRValueType();
        RConst defaultValue = null;
        if(current.type == tAssign){
            next();//skip '='
            defaultValue = parseConst();
        }
        skipEndOfStatement();
        return new RProperty(name,valueType,defaultValue,annotations);
    }

    /**
     * Parse a value literal.<br>
     * @return The literal.
     */
    private RConst parseConst(){
        switch (current.type){
            case tArrayStart: return parseValueArray();
            case tTrue:
            case tFalse:
                return parseValueBoolean();
            case tMin:
            case tPlus:
            case tNumber:   return parseValueNumber();
            case tNew: return parseValueValueObject();
            case tIdentifier: return parserValueEnum();
            case tNull: next(); return RConstNull.Null;
            case tString: {
                String value = current.text;
                next();
                return new RConstString(value.substring(1,value.length()-1));
            }
            default:
                throw new SubstemaParserException(current.pos,"Expected a literal value");
        }
    }
    private RConstValueObject parseValueValueObject() {
        skip(tNew,"'new' expected");
        RClass name = parseRClass("");
        skip(tOpen,"'(' expected after value class name");
        POrderedMap<String,RConst> args =   POrderedMap.empty();
        if(current.type != tClose){
            args = args.plusAll(sep(tComma,() -> {
                String propName = current.text;
                skip(tIdentifier,"Expected property name.");
                skip(tColon,"':' expected after property name.");
                return new Tuple2<>(propName, parseConst());
            }));
        }
        skip(tClose,"')' expected after value class aruments");
        return new RConstValueObject(new RTypeSig(name),args);
    }
    private RConstEnum parserValueEnum() {
        RClass cls = parseRClass(packageName);
        skip(SubstemaTokenType.tPoint,"'.' expected after enum name");
        String valueName = current.text;
        skip(tIdentifier,"enum value name expected");
        return new RConstEnum(cls,valueName);
    }

    private RConstNumber parseValueNumber() {
        boolean negative = false;
        if(current.type == tPlus){
            next();
        } else if(current.type == tMin){
            negative = true;
            next();
        }
        if(current.type != tNumber){
            throw new SubstemaParserException(current.pos,"Expected a number");
        }
        RClass cls = SubstemaUtils.integerRClass;
        String txt = (negative ? "-" : "") + current.text.toLowerCase();
        Function2<Long,Long,Boolean> check = (min, max) -> {
            long value = Long.parseLong(txt);
            return min <= value && value <= max;
        };
        Number value;
        if(txt.endsWith("l")){
            cls = SubstemaUtils.longRClass;
            value = Long.parseLong(StringUtils.dropLast(txt,1));
        } else if(txt.contains("f")){
            cls = SubstemaUtils.floatRClass;
            value = Float.parseFloat(StringUtils.dropLast(txt,1));
        } else if(txt.contains("s")){
            cls = SubstemaUtils.shortRClass;
            if(check.apply((long)Short.MIN_VALUE,(long)Short.MAX_VALUE) == false){
                throw new SubstemaParserException(current.pos,"Value " + txt + " is to big or to small to be a Short");
            }
            value = Short.parseShort(StringUtils.dropLast(txt,1));
        } else if(txt.contains("b")){
            cls = SubstemaUtils.byteRClass;
            if(check.apply((long)Byte.MIN_VALUE,(long)Byte.MAX_VALUE) == false){
                throw new SubstemaParserException(current.pos,"Value " + txt + " is to big or to small to be a Byte");
            }
            value = Byte.parseByte(StringUtils.dropLast(txt,1));
        } else if(txt.contains("d") || txt.contains(".")){
            cls = SubstemaUtils.doubleRClass;
            value = Double.parseDouble(StringUtils.dropLast(txt,1));
        } else {
            if(check.apply((long)Integer.MIN_VALUE,(long)Integer.MAX_VALUE) == false){
                cls = SubstemaUtils.longRClass;
                value = Long.parseLong(txt);
            } else {
                value = Integer.parseInt(txt);
            }

        }
        next(); //skip number
        return new RConstNumber(cls,value);
    }
    private RConstArray parseValueArray() {
        skip(tArrayStart,"'[' expected");
        PList<RConst>   elements = PList.empty();
        if(current.type != tArrayEnd){
            elements =sep(tComma,this::parseConst);
        }
        skip(tArrayEnd,"']' expected");
        return new RConstArray(elements);
    }

    private RConstBoolean parseValueBoolean() {
        if(current.type == tTrue){
            next();
            return new RConstBoolean(true);
        }
        if(current.type == tFalse){
            next();
            return new RConstBoolean(false);
        }
        throw new SubstemaParserException(current.pos,"Expected a boolean value");
    }

    private RFunctionParam  parseFunctionParam(PList<RAnnotation> annotations) {
        assertType(tIdentifier,"parameter name expected");
        String name = current.text;
        next(); //skip name;

        skip(tColon,"':' expected after parameter name");
        RValueType valueType = parseRValueType();

        return new RFunctionParam(name,valueType,annotations);
    }

    private RRemoteClass parseRemoteClass(PList<RAnnotation> annotations) {
        skip(tRemote,"'remote' expected");
        skip(tClass,"'class' expected");
        assertType(tIdentifier,"function name expected");
        RClass name = new RClass(packageName,current.text);
        next(); //skip name;

        PList<RFunction> functions = PList.empty();
        if(current.type == tBlockStart){
            next();
            while(current.type!= tEOF && current.type != tBlockEnd){
                functions = functions.plus(parseRFunction(parseAnnotations()));
            }
            skip(tBlockEnd,"'}' expected");
        }
        return new RRemoteClass(name,functions,annotations);
    }

    private RFunction parseRFunction(PList<RAnnotation> annotations) {
        assertType(tIdentifier,"function name expected");
        String name = current.text;
        next(); //skip name;
        skip(tOpen,"'(' expected after function name");
        PList<RFunctionParam> params = PList.empty();
        if(current.type == tIdentifier) {
            params = sep(tComma, () -> parseFunctionParam(parseAnnotations()));
        }
        skip(tClose,"')' expected after function parameters");
        skip(tColon,"':' expected to define the function return type");
        RValueType returnType = null;
        boolean cached = false;
        if(current.type != tVoid){
            returnType = parseRValueType();
            if(current.type == tCached) {
                if(params.isEmpty() == false){
                    throw new SubstemaParserException(current.pos,"cached result is not supported on functions with parameters.");
                }
                cached = true;
                next(); //skip cached
            }
        } else {
            next();//skip void
        }

        skipEndOfStatement();
        return new RFunction(name,params,returnType,cached,annotations);
    }

    private String parsePackageName() {
        //skip(tPackage,"package expected");
        String res = sep(tPoint,() -> {
            assertType(tIdentifier,"name expected");
            String name = current.text;
            next();
            return name;
        }).toString(".");

        return res;
    }

    private void skipEndOfStatement() {
        skip(tSemiColon,"';' expected after statement.");
    }


    /**
     * Parse a list of values, separated by the supplied seperation token type.<br>
     * @param sep   The token type of the seperator
     * @param r The parser for 1 value
     * @param <T>   The type of the value parsers.
     * @return the list of parsed items.
     */
    private <T> PList<T> sep(SubstemaTokenType sep, Supplier<T> r){
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

    private REnum parseEnum(PList<RAnnotation> annotations){
        skip(tEnum,"'enum' expected");
        assertType(tIdentifier,"enum name expected.");
        String name = current.text;
        next();
        skip(tBlockStart,"'{' expected for enum definition");
        PList<String> values = PList.empty();
        if(current.type != tSemiColon) {
                values = sep(tComma, () -> {
                assertType(tIdentifier, "enum value name expected");
                String valueName = current.text;
                next();
                return valueName;
            });
        }
        next();//skip ;
        skip(tBlockEnd,"'}' expected to end enum definintion for '" + name + "'");
        return new REnum(new RClass(packageName,name),values,annotations);
    }

    private RTypeSig    replaceType(RTypeSig type, RClass genericName, RTypeSig genericType){
        if(type.getName().equals(genericName)){
            return genericType;
        }
        PList<RTypeSig> newGen = type.getGenerics().map(gt -> replaceType(gt,genericName,genericType));
        return new RTypeSig(type.getName(),newGen);
    }



    static public void main(String...args) throws Exception{
        String test = PList.val(
                "package be.schaubroeck;",
                "enum Runtime{",
                " production, development",
                "}",
                "value class AppInfo{",
                "name:String;",
                "version:String;",
                "runtime:Runtime;",
                "}",
                "remote class App{",
                "getAppInfo():AppInfo;",
                "}"
                ).toString("\n");
        System.out.println(test);
        PList<Token<SubstemaTokenType>> tokens = new SubstemaTokenizer().tokenize("test.rod",test);
        tokens.forEach(System.out::println);
        new SubstemaParser("com.undefined",tokens).parseSubstema();
    }
}
