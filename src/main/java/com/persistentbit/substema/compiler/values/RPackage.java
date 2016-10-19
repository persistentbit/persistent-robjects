package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;

/**
 * RPackage can be the first thing defined in a Substema and
 * can contain package specific documentation and other annotations
 */
public class RPackage {
    private PList<RAnnotation> annotations;

    public RPackage(PList<RAnnotation> annotations) {
        this.annotations = annotations;
    }

    public PList<RAnnotation> getAnnotations() {
        return annotations;
    }


}
