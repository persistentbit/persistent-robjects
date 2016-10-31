package com.persistentbit.substema.substemagen;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.sourcegen.SourceGen;
import com.persistentbit.substema.compiler.SubstemaUtils;
import com.persistentbit.substema.compiler.values.*;
import com.persistentbit.substema.compiler.values.expr.RConst;

/**
 * Create substema source code from compiled substema ({@link RSubstema},{@link RValueClass},...)
 *
 * @author petermuys
 * @see RSubstema
 * @since 31/10/16
 */
public class SubstemaSourceGenerator extends SourceGen{

	public void addSubstema(RSubstema substema) {
		addPackage(substema.getPackageDef());

		//imports
		substema.getImports().forEach(this::addImport);
		println("");

		//Annotation defs
		substema.getAnnotationDefs().forEach(this::addAnnotationDef);
		println("");

		//Enums
		substema.getEnums().forEach(this::addEnum);
		println("");

		//Interfaces
		substema.getInterfaceClasses().forEach(this::addInterface);
		println("");

		//Value Classes
		substema.getValueClasses().forEach(this::addValueClass);
		println("");

		//Remote Classes
		substema.getRemoteClasses().forEach(this::addRemote);
		println("");

	}

	public void addPackage(RPackage p) {

		addAnnotations(p.getAnnotations());
		println("package;");
	}

	public void addAnnotations(PList<RAnnotation> annotationList) {
		annotationList.forEach(a -> {
			if(a.getName().equals(SubstemaUtils.docRClass)) {
				println("<<");
				a.getValues().values().forEach(c -> println(toStringRConst(c)));
				println(">>");
			}
			else {
				String props = a.getValues().map(t ->
													 t._1 + " = " + toStringRConst(t._2)
				).toString(", ");
				if(props.isEmpty() == false) {
					props = "(" + props + ")";
				}
				println("@" + a.getName() + props + ";");
			}
		});
	}

	public String toStringRConst(RConst rconst) {
		return rconst.toString();
	}

	public void addRemote(RRemoteClass r) {
		addAnnotations(r.getAnnotations());
		bs("remote class " + r.getName().getClassName());
		r.getFunctions().forEach(this::addFunction);
		be();
	}

	public void addFunction(RFunction f) {
		addAnnotations(f.getAnnotations());
		println(f.getName() +
					"(" + f.getParams().map(this::toStringFunctionParam).toString(", ") + "):" +
					f.getResultType().map(RValueType::toSource).orElse("void") +
					(f.isCached() ? " cached;" : ";")
		);
	}

	public String toStringFunctionParam(RFunctionParam fp) {
		//todo Add annotations for parameter
		return fp.getName() + ":" + fp.getValueType().toSource();
	}

	public void addImport(RImport i) {
		println("import " + i.getPackageName() + ";");
	}

	public void addValueClass(RValueClass vc) {
		addAnnotations(vc.getAnnotations());
		String impl = vc.getInterfaceClasses().map(RClass::getClassName).toString(", ");
		if(impl.isEmpty() == false) {
			impl = " implements " + impl;
		}
		bs("case class " + vc.getTypeSig().toSource() + impl);
		vc.getProperties().forEach(this::addProperty);
		be();
	}

	public void addInterface(RInterfaceClass i) {
		addAnnotations(i.getAnnotations());
		bs("interface " + i.getName().getClassName());
		i.getProperties().forEach(this::addProperty);
		be();
	}

	public void addEnum(REnum e) {
		addAnnotations(e.getAnnotations());
		bs("enum " + e.getName().getClassName());
		println(e.getValues().toString(", ") + ";");
		be();
	}

	public void addAnnotationDef(RAnnotationDef at) {
		addAnnotations(at.getAnnotations());
		bs("annotation " + at.getName().getClassName() + "{");
		at.getProperties().forEach(this::addProperty);
		be();
	}

	public void addProperty(RProperty p) {
		addAnnotations(p.getAnnotations());
		String defaultValue = p.getDefaultValue().map(c -> " = " + c.toSource()).orElse("");
		println(p.getName() + ": " + p.getValueType().toSource() + defaultValue + ";");
	}

	@Override
	public String toString() {
		return writeToString();
	}
}
