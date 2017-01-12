package com.persistentbit.substema.codegen;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;
import com.persistentbit.substema.compiler.SubstemaCompiler;
import com.persistentbit.substema.compiler.values.RSubstema;
import com.persistentbit.substema.dependencies.DependencySupplier;
import com.persistentbit.substema.dependencies.SupplierDef;
import com.persistentbit.substema.dependencies.SupplierType;
import com.persistentbit.substema.javagen.GeneratedJava;
import com.persistentbit.substema.javagen.JavaGenOptions;
import com.persistentbit.substema.javagen.SubstemaJavaGen;

/**
 * Test the Substema Code gen functionality
 *
 * @author petermuys
 * @since 31/10/16
 */
public class SubstemaCodeGenTest{

	static final TestCase sourceGen = TestCase.name("Substem source code generator").code(tr -> {

		generateCode(tr, "com.persistentbit.substema.tests.codegentest");
		generateCode(tr, "com.persistentbit.substema.tests.compiler.enums");
		generateCode(tr, "com.persistentbit.substema.tests.compiler.annotations");

	});


	private static void generateCode(TestRunner tr, String destPackage) {
		tr.info("Testing " + destPackage);
		DependencySupplier ds =
			new DependencySupplier(PList.val(new SupplierDef(SupplierType.resource, "/")));
		SubstemaCompiler             comp      = new SubstemaCompiler(ds);
		RSubstema                    substema  = comp.compile(destPackage).orElseThrow();
		PList<Result<GeneratedJava>> generated =
			SubstemaJavaGen.generate(comp, new JavaGenOptions(true, true), substema);

		generated.forEach(gj ->
							  tr.isSuccess(gj)
		);
	}

	public void testAll() {
		TestRunner.runAndPrint(SubstemaTestUtils.testLogPrinter, SubstemaCodeGenTest.class);
	}


	public static void main(String[] args) {
		new SubstemaCodeGenTest().testAll();
	}
}
