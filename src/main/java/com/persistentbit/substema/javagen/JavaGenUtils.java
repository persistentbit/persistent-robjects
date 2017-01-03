package com.persistentbit.substema.javagen;

import com.persistentbit.core.collections.PByteList;
import com.persistentbit.substema.compiler.SubstemaUtils;
import com.persistentbit.substema.compiler.values.RClass;
import com.persistentbit.substema.compiler.values.RTypeSig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Substema to Java code generator utilities.<br>
 *
 * @author Peter Muys
 * @since 30/09/16
 */
public final class JavaGenUtils{


	public static String toString(String defaultPackage, Class<?> cls) {
		return toString(defaultPackage, toRClass(cls));
	}

	public static String toString(String defaultPackage, RClass cls) {
		String clsName        = cls.getClassName();
		String clsPackageName = cls.getPackageName();

		if(clsPackageName.equals(defaultPackage) || clsPackageName.isEmpty()) {

			if(clsName.equals(SubstemaUtils.dateRClass.getClassName())) {
				return LocalDate.class.getSimpleName();
			}

			if(clsName.equals(SubstemaUtils.dateTimeRClass.getClassName())) {
				return LocalDateTime.class.getSimpleName();
			}
			if(clsName.equals(SubstemaUtils.binaryRClass.getClassName())){
				return PByteList.class.getSimpleName();
			}
			return clsName;
		}
		return cls.getPackageName() + "." + clsName;
	}

	public static RClass toRClass(Class<?> cls) {
		return new RClass(cls.getPackage().getName(), cls.getSimpleName());
	}

	public static String toString(String defaultPackage, RTypeSig typeSig) {
		RClass cls = typeSig.getName();
		String res;
		if(cls.getPackageName().equals(defaultPackage) || cls.getPackageName().isEmpty()) {
			res = cls.getClassName();
		}
		else {
			res = cls.getPackageName() + "." + cls.getClassName();
		}
		String gen = typeSig.getGenerics().map(g -> toString(defaultPackage, g)).toString(",");
		res += gen.isEmpty() ? "" : "<" + gen + ">";
		return res;
	}

	public static Optional<String> genericsToString(String defaultPackage, RTypeSig typeSig) {
		String res = typeSig.getGenerics().map(g -> toString(defaultPackage, g)).toString(", ");
		return res.isEmpty() ? Optional.empty() : Optional.of(res);
	}
}
