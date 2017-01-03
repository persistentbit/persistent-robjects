package com.persistentbit.substema.dependencies;

import com.persistentbit.core.utils.BaseValueClass;

import java.util.Optional;

/**
 * Defines a Substema Source Supplier
 * @author Peter Muys
 * @since 25/09/16
 */
public class SupplierDef extends BaseValueClass{
    private final SupplierType type;
    private final String       path;
    private final String       sourceCode;

    public SupplierDef(SupplierType type, String path) {
        this(type, path, null);
    }

    public SupplierDef(SupplierType type, String path, String sourceCode) {
        this.type = type;
        this.path = path;
        this.sourceCode = sourceCode;
    }

    public SupplierType getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public Optional<String> getSource() { return Optional.ofNullable(sourceCode); }
}
