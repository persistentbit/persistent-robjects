package com.persistentbit.substema;

import com.persistentbit.core.collections.PList;
import com.persistentbit.jjson.nodes.JJPrinter;
import com.persistentbit.substema.compiler.SubstemaCompiler;
import com.persistentbit.substema.compiler.values.RSubstema;
import com.persistentbit.substema.dependencies.DependencySupplier;
import com.persistentbit.substema.dependencies.SupplierDef;
import com.persistentbit.substema.dependencies.SupplierType;
import org.junit.Test;

/**
 * Created by petermuys on 6/10/16.
 */
public class TestSubstemaCompiler {
    @Test
    public void testAnnotations() {
        DependencySupplier ds = new DependencySupplier(PList.val(new SupplierDef(SupplierType.resource,"/")));
        SubstemaCompiler compiler = new SubstemaCompiler(ds);
        RSubstema substema = compiler.compile("com.persistentbit.substema.tests.compiler.annotations");
        System.out.println(JJPrinter.toJson(substema));
    }
}
