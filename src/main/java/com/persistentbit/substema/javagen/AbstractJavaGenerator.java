package com.persistentbit.substema.javagen;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.sourcegen.SourceGen;
import com.persistentbit.core.utils.UString;
import com.persistentbit.substema.compiler.AnnotationsUtils;
import com.persistentbit.substema.compiler.SubstemaCompiler;
import com.persistentbit.substema.compiler.SubstemaUtils;
import com.persistentbit.substema.compiler.values.RAnnotation;
import com.persistentbit.substema.compiler.values.RClass;

import java.time.LocalDateTime;

/**
 * @since 7/10/16
 * @author Peter Muys
 */
public class AbstractJavaGenerator extends SourceGen{
    private PSet<RClass>    imports = PSet.empty();
    private SourceGen       header = new SourceGen();
    protected String          packageName;
    protected final AnnotationsUtils atUtils;
    public AbstractJavaGenerator(SubstemaCompiler compiler,String packageName) {
        this.packageName = packageName;
        atUtils = new AnnotationsUtils(compiler);

        header.println("// WARNING !");
        header.println("// GENERATED CODE FOR SUBSTEMA PACKAGE " + packageName);
        header.println("// See resource file " + packageName+".substema for the definition.");
        header.println("// generated on " + LocalDateTime.now());
        header.println("");
    }

    /**
     * Create a java header with comments and all the external imports.<br>
     * Add the generated java source.<br>
     * Create and return a {@link GeneratedJava} instance
     * @param cls   The {@link RClass} that this represents
     * @return The new Generated java source.
     */
    protected Result<GeneratedJava> toGenJava(RClass cls) {
        return Result.function(cls.getFullName()).code(l -> {
            //Create the header and add it to this SourcGen instance
            SourceGen sg = new SourceGen();
            header.println("package " + packageName + ";");
            header.println("");
            sg.add(header);

            //Add all the external Imports.
            imports.filter(i -> i.getPackageName().equals(packageName) == false)
                .forEach(i -> sg.println("import " + i.getPackageName() + "." + i.getClassName() + ";"));
            sg.println("");


            //Add this generated source to the result
            sg.add(this);

            return sg.writeToString().map(str -> new GeneratedJava(cls, str));
        });
    }

    protected void addImport(RClass cls){
        imports = imports.plus(cls);
    }
    protected void addImport(Class<?> cls){
        addImport(new RClass(cls.getPackage().getName(),cls.getSimpleName()));
    }
    protected void generateJavaDoc(PList<RAnnotation> allAnnotations){
        generateJavaDoc(allAnnotations, "");
    }

    protected void generateJavaDoc(PList<RAnnotation> allAnnotations, String extra) {
        //Get all documentation strings
        PStream<String> docs =  atUtils.getAnnotation(allAnnotations, SubstemaUtils.docRClass).lazy()
									   .map(a -> atUtils.getStringProperty(a,"info").orElse(null))
									   .filterNulls()
									   .map(UString::unEscapeJavaString)
									   .map(UString::splitInLines).<String>flatten()
			.plist();


        if(docs.isEmpty() == false){
            println("/**");
			docs.forEach(d -> println(" * " + d));
			UString.splitInLines(extra).forEach(l -> println(" * " + l));
			println(" */");
        }
    }

}
