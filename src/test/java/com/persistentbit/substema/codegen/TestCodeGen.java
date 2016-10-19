package com.persistentbit.substema.codegen;

import com.persistentbit.core.collections.PList;
import com.persistentbit.substema.compiler.SubstemaCompiler;
import com.persistentbit.substema.compiler.values.RSubstema;
import com.persistentbit.substema.dependencies.DependencySupplier;
import com.persistentbit.substema.dependencies.SupplierDef;
import com.persistentbit.substema.dependencies.SupplierType;
import com.persistentbit.substema.javagen.GeneratedJava;
import com.persistentbit.substema.javagen.JavaGenOptions;
import com.persistentbit.substema.javagen.SubstemaJavaGen;
import org.junit.Test;

/**
 * Created by petermuys on 16/09/16.
 */
public class TestCodeGen {


    @Test
    public void testCodeGenExternamEnum() throws Exception{
        generateCode(new JavaGenOptions(true,true),"com.persistentbit.substema.tests.compiler.enums");
    }

    @Test
    public void testCodeGen() throws Exception{
        /*generateCode(new JavaGenOptions(),"com.persistentbit.generated.defaultOptions");
        generateCode(new JavaGenOptions().withGenerateGetters(true),"com.persistentbit.generated.withGetters");
        generateCode(new JavaGenOptions().withGenerateUpdaters(true),"com.persistentbit.generated.withUpdaters");
        generateCode(new JavaGenOptions().withGenerateUpdaters(true).withGenerateGetters(true),"com.persistentbit.generated.generateAll");
*/

    }

    public void generateCode(JavaGenOptions options,String destPackage) throws Exception{

        DependencySupplier ds = new DependencySupplier(PList.val(new SupplierDef(SupplierType.resource,"/")));
        SubstemaCompiler    comp = new SubstemaCompiler(ds);
        RSubstema substema = comp.compile(destPackage);
        PList<GeneratedJava> generated = SubstemaJavaGen.generate(comp,options,substema);
        generated.forEach(gj -> System.out.println(gj.code));
        /*String rodFileName= "codeGenTest.rod";
        URL url = SubstemaJavaGen.class.getResource("/" + rodFileName);
        System.out.println("URL: " + url);
        Path path = Paths.get(url.toURI());
        System.out.println("Path  = " + path);
        String rod = new String(Files.readAllBytes(path));
        SubstemaTokenizer tokenizer = new SubstemaTokenizer();
        PList<Token<SubstemaTokenType>> tokens = tokenizer.tokenize(rodFileName,rod);
        SubstemaParser parser = new SubstemaParser(destPackage,tokens);
        RSubstema service = parser.parseSubstema();
        System.out.println(service);
        PList<GeneratedJava> gen = SubstemaJavaGen.generate(new JavaGenOptions(),destPackage,service);
        Path srcPath = SourcePath.findTestSourcePath(SubstemaJavaGen.class, rodFileName);

        Path packagePath = srcPath.toAbsolutePath().resolve(destPackage.replace('.', File.separatorChar));
        Files.createDirectories(packagePath);
        gen.forEach(gj -> {
            Path filePath = packagePath.resolve(gj.name.getClassName() + ".java");

            System.out.println("File: " + filePath.toString());
            try {
                Files.write(filePath,gj.code.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(gj.code);
            System.out.println("-----------------------------------");

        });
        */
    }
}
