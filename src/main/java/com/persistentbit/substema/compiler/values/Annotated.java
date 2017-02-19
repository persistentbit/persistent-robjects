package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;

/**
 * TODOC
 *
 * @author petermuys
 * @since 19/02/17
 */
public interface Annotated<R extends Annotated>{

	R withAnnotations(PList<RAnnotation> annotations);

	PList<RAnnotation> getAnnotations();
}
