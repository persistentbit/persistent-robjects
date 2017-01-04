package com.persistentbit.substema.codegen;

import com.persistentbit.substema.compiler.SubstemaCompiler;
import com.persistentbit.substema.compiler.values.RSubstema;
import com.persistentbit.substema.dependencies.DependencySupplier;
import com.persistentbit.substema.substemagen.SubstemaSourceGenerator;
import org.junit.Test;

/**
 * Test the Substema Code gen functionality
 *
 * @author petermuys
 * @since 31/10/16
 */
public class SubstemaCodeGenTest{

	@Test
	public void testSubstameSourceGen() {
		String             packageName = "com.persistentbit.substema.tests.codegentest";
		DependencySupplier depSupplier = new DependencySupplier().withResources();
		SubstemaCompiler   compiler    = new SubstemaCompiler(depSupplier);
		RSubstema          substema    = compiler.compile(packageName).orElseThrow();

		SubstemaSourceGenerator sourceGen = new SubstemaSourceGenerator();
		sourceGen.addSubstema(substema);
		String source = sourceGen.writeToString();
		System.out.println(source);
		RSubstema version2 =
			new SubstemaCompiler(new DependencySupplier().withSource(packageName, source)).compile(packageName)
				.orElseThrow();
	}
}
