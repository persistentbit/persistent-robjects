package com.persistentbit.substema.dependencies;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Optional;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by petermuys on 25/09/16.
 */

public class DependencySupplier implements Function<String,Optional<String>> {
    static public final String substemaDefFileExtension = ".substema";
    private final PList<SupplierDef> suppliers;
    private PMap<String,String> resolved = PMap.empty();

    public DependencySupplier(PList<SupplierDef> suppliers) {
        this.suppliers = suppliers;
    }

    @Override
    public Optional<String> apply(String packageName) {
        if(resolved.containsKey(packageName) == false){
            resolve(packageName);
        }
        return resolved.getOpt(packageName);
    }

    private void resolve(String packageName){
        String found = null;
        for(SupplierDef def : suppliers){
            switch (def.getType()){
                case archive: found = resolveArchive(def,packageName);break;
                case folder: found = resolveFolder(def,packageName);break;
                default: throw new RuntimeException("Unknown dependency supplier type: " + def.getType());
            }
            if(found != null){
                resolved = resolved.put(packageName,found);
                break;
            }
        }
    }
    private String resolveArchive(SupplierDef def, String packageName){
        File f = new File(def.getPath());
        try(ZipFile zipFile = new ZipFile(f,ZipFile.OPEN_READ)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            /*while(entries.hasMoreElements()){
                System.out.println(entries.nextElement());
            }*/
            ZipEntry entry = zipFile.getEntry(packageName + substemaDefFileExtension);
            if(entry != null){
                StringBuffer res = new StringBuffer();
                try(Reader r = new InputStreamReader(zipFile.getInputStream(entry), Charset.forName("UTF-8"))){
                    while(true){
                        int c = r.read();
                        if(c == -1){
                            break;
                        }
                        res.append((char)c);
                    }

                }
                return res.toString();
            }
            return null;

        } catch (IOException e) {
            throw new RuntimeException("Error handling archive " + f.getAbsolutePath(),e);
        }
    }

    private String resolveFolder(SupplierDef def, String packageName){
        File f = new File(def.getPath(),packageName + substemaDefFileExtension);
        if(f.exists()){
            try {
                return new String(Files.readAllBytes(f.toPath()));
            } catch (IOException e) {
                throw new RuntimeException("Error reading file " + f.getAbsolutePath(),e);
            }
        }
        return null;
    }

    /*
    static public void main(String...args){
        SupplierDef def1 = new SupplierDef(SupplierType.archive,"/Users/petermuys/.m2/repository/com/persistentbit/substema/1.0.0-SNAPSHOT/substema-1.0.0-SNAPSHOT.jar");
        SupplierDef def2 =new SupplierDef(SupplierType.folder,"/Users/petermuys/develop/persstentbit/substema-api/src/main/resources");
        DependencySupplier sup = new DependencySupplier(PList.val(def1,def2));
        //System.out.println(sup.apply("com.persistentbit.parser"));
        System.out.println(sup.apply("com.persistentbit.substema.api"));

    }*/
}
