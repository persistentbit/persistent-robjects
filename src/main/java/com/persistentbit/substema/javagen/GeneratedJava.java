package com.persistentbit.substema.javagen;

import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.IO;
import com.persistentbit.substema.compiler.values.RClass;

import java.io.File;

/**
 * @since 14/09/16
 * @author Peter Muys
 */
public class GeneratedJava {
    public final RClass name;
    public final String code;

    public GeneratedJava(RClass name,  String code) {
        this.name = name;
        this.code = code;
    }

    @Override
    public String toString() {
        return "GeneratedJava(" + name + ")";
    }

    public File getPackagePath(File root) {
        return name.getPackagePath(root);
    }


    public Result<File> writeToFile(File root) {
        return Result.function(this, root).code(l ->

                                                    IO.mkdirsIfNotExisting(getPackagePath(root))
                                                        .map(dest -> new File(name.getClassName() + ".java"))
                                                        .map(dest -> {
                                                            l.info("Writing Generated java to " + dest
                                                                .getAbsolutePath());
                                                            IO.writeFile(code, dest, IO.utf8);
                                                            return dest;
                                                        })
        );

    }

}