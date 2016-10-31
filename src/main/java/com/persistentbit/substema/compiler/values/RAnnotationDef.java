package com.persistentbit.substema.compiler.values;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Contains the definition of a Substema Annotation
 *
 * @author Peter Muys
 * @since 6/10/16
 */
public class RAnnotationDef extends BaseValueClass{

	private final RClass             name;
	private final PList<RProperty>   properties;
	private final PList<RAnnotation> annotations;

	public RAnnotationDef(RClass name, PList<RProperty> properties, PList<RAnnotation> annotations) {
		this.name = name;
		this.properties = properties;
		this.annotations = annotations;
	}

	public PList<RProperty> getProperties() {
		return properties;
	}

	public RClass getName() {
		return name;
	}

	public PList<RAnnotation> getAnnotations() { return annotations; }

	public RAnnotationDef withName(RClass name) {
		return copyWith("name", name);
	}

	public RAnnotationDef withProperties(PList<RProperty> properties) {
		return copyWith("properties", properties);
	}

	public RAnnotationDef withAnnotations(PList<RAnnotation> annotations
	) { return copyWith("annotations", annotations); }
}