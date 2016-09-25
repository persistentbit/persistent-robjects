package com.persistentbit.substema.dependencies;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * Created by petermuys on 25/09/16.
 */
public class SupplierDef extends BaseValueClass{
    private final SupplierType type;
    private final String path;

    public SupplierDef(SupplierType type, String path) {
        this.type = type;
        this.path = path;
    }

    public SupplierType getType() {
        return type;
    }

    public String getPath() {
        return path;
    }
}
