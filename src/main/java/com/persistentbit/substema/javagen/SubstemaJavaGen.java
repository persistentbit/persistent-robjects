package com.persistentbit.substema.javagen;


import com.persistentbit.core.Nullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.logging.PLog;
import com.persistentbit.core.sourcegen.SourceGen;
import com.persistentbit.core.utils.builders.NOT;
import com.persistentbit.core.utils.builders.SET;
import com.persistentbit.substema.annotations.Remotable;
import com.persistentbit.substema.annotations.RemoteCache;
import com.persistentbit.substema.compiler.SubstemaCompiler;
import com.persistentbit.substema.compiler.SubstemaException;
import com.persistentbit.substema.compiler.values.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Generate Java code for a Substema
 *
 * @since 14/09/16
 */
public final class SubstemaJavaGen{

	private static final PLog log = PLog.get(SubstemaJavaGen.class);
	private final JavaGenOptions options;
	private final RSubstema      substema;

	private final SubstemaCompiler compiler;
	private final PList<GeneratedJava> generatedJava = PList.empty();


	/**
	 * Internal constructor for the java generator
	 *
	 * @param compiler The compiler used for external dependencies
	 * @param options  The code generator options
	 * @param substema The main substema
	 */
	private SubstemaJavaGen(SubstemaCompiler compiler, JavaGenOptions options, RSubstema substema) {
		this.options = options;
		this.substema = substema;
		this.compiler = compiler;

	}

	/**
	 * Generate java using {@link #generate(SubstemaCompiler, JavaGenOptions, RSubstema)} and
	 * then write the files to a local folder
	 *
	 * @param compiler     The compiler for external definitions
	 * @param options      The java generator options
	 * @param substema     The substema to generate java classes for
	 * @param outputFolder The root folder where the generated java will be written under
	 */
	public static void generateAndWriteToFiles(SubstemaCompiler compiler, JavaGenOptions options, RSubstema substema,
											   File outputFolder
	) {
		PList<GeneratedJava> result = generate(compiler, options, substema);

		result.forEach(g -> {
			//Package name to path string
			String packagePath = g.name
				.getPackageName()
				.replace('.', File.separatorChar);
			//create folders...
			File dest = new File(outputFolder, packagePath);
			if(dest.exists() == false) { dest.mkdirs(); }


			dest = new File(dest, g.name.getClassName() + ".java");
			log.info("Generating " + dest.getAbsolutePath());
			try(FileWriter fw = new FileWriter(dest)) {
				fw.write(g.code);
			} catch(IOException io) {
				log.error(io);
				throw new RuntimeException("Can't write to " + dest.getAbsolutePath());
			}
		});
	}

	/**
	 * Generate all the java classes for a given substema and
	 * returns them as a list
	 *
	 * @param compiler The compiler for external definitions
	 * @param options  The java generator options
	 * @param substema The substema to generate java classes for
	 *
	 * @return The List of {@link GeneratedJava} java sources
	 */
	public static PList<GeneratedJava> generate(SubstemaCompiler compiler, JavaGenOptions options, RSubstema substema) {
		return new SubstemaJavaGen(compiler, options, substema).generateSubstema();
	}

	/**
	 * Main function of the java generator
	 *
	 * @return The list of {@link GeneratedJava} sources
	 */
	public PList<GeneratedJava> generateSubstema() {
		PList<GeneratedJava> result = PList.empty();
		if(substema.getPackageDef().getAnnotations().isEmpty() == false) {
			result = result.plus(new Generator().generatePackageInfo(substema.getPackageDef()));

		}

		result = result.plusAll(
			substema.getEnums()
				.map(e -> new Generator().generateEnum(e)));
		result = result.plusAll(
			substema.getValueClasses()
				.map(vc -> new Generator().generateValueClass(vc)));
		result = result.plusAll(
			substema.getRemoteClasses()
				.map(rc -> new Generator().generateRemoteClass(rc)));
		result = result.plusAll(
			substema.getInterfaceClasses()
				.map(ic -> new Generator().generateInterfaceClass(ic)));

		return result.filterNulls().plist();
	}


	private final class Generator extends AbstractJavaGenerator{

		private Generator() {
			super(compiler, substema.getPackageName());
		}

		public GeneratedJava generatePackageInfo(RPackage packageDef) {
			//Create the header and add it to this SourceGen instance
			SourceGen sg = new SourceGen();
			generateJavaDoc(packageDef.getAnnotations());
			sg.add(this);
			sg.println("package " + packageName + ";");

			sg.println("");
			return new GeneratedJava(new RClass(packageName, "package-info"), sg.writeToString());

		}

		public GeneratedJava generateEnum(REnum e) {
			generateJavaDoc(e.getAnnotations());
			bs("public enum " + e.getName().getClassName());
			{
				println(e.getValues().toString(","));
			}
			be();
			return toGenJava(e.getName());
		}


		public GeneratedJava generateInterfaceClass(RInterfaceClass ic) {
			generateJavaDoc(ic.getAnnotations());
			bs("public interface " + ic.getName().getClassName());
			{
				//****** GETTERS AND UPDATERS
				ic.getProperties().forEach(p -> {
					if(options.generateGetters) {
						String rt = toString(p.getValueType().getTypeSig(), p.getValueType().isRequired());
						String vn = p.getName();
						if(p.getValueType().isRequired() == false) {
							addImport(Optional.class);
							rt = "Optional<" + rt + ">";
							vn = "Optional.ofNullable(" + vn + ")";
						}
						println("public " + rt + " get" + firstUpper(p.getName()) + "();");
					}
					if(options.generateUpdaters) {
						String s = "public " + ic.getName()
							.getClassName() + " with" + firstUpper(p.getName()) + "(" + toString(p.getValueType()
																									 .getTypeSig(), p
																									 .getValueType()
																									 .isRequired()) + " " + p
							.getName() + ");";

						println(s);
					}
					if(options.generateGetters || options.generateUpdaters) {
						println("");
					}
				});
			}
			be();
			return toGenJava(ic.getName());
		}

		private String toString(RTypeSig sig, boolean asPrimitive) {
			String gen =
				sig.getGenerics().isEmpty() ? "" : sig.getGenerics().map(this::toString).toString("<", ",", ">");
			String packName = sig.getName().getPackageName();
			String name     = sig.getName().getClassName();

			switch(name) {
				case "List":
					name = "PList";
					addImport(PList.class);
					break;
				case "Set":
					name = "PSet";
					addImport(PSet.class);
					break;
				case "Map":
					name = "PMap";
					addImport(PMap.class);
					break;
				case "Date":
					name = "LocalDate";
					addImport(LocalDate.class);
					break;
				case "DateTime":
					name = "LocalDateTime";
					addImport(LocalDateTime.class);
					break;

				case "Boolean":
					name = asPrimitive ? "boolean" : name;
					break;
				case "Byte":
					name = asPrimitive ? "byte" : name;
					break;
				case "Short":
					name = asPrimitive ? "short" : name;
					break;
				case "Integer":
					name = asPrimitive ? "int" : name;
					break;
				case "Long":
					name = asPrimitive ? "long" : name;
					break;
				case "Float":
					name = asPrimitive ? "float" : name;
					break;
				case "Double":
					name = asPrimitive ? "double" : name;
					break;

				case "String":
					break;

				default:
					if(packName.isEmpty()) {
						throw new SubstemaException("Don't know internal class " + name);
					}
					addImport(new RClass(packName, name));
					break;
			}

			return name + gen;
		}

		private String firstUpper(String s) {
			return s.substring(0, 1).toUpperCase() + s.substring(1);
		}

		/**
		 * Generate java for a Case Class (or value class)
		 *
		 * @param vc The RValueClass to generate code for
		 *
		 * @return The generated Java code
		 */
		public GeneratedJava generateValueClass(RValueClass vc) {
			generateJavaDoc(vc.getAnnotations());


			String impl = vc.getInterfaceClasses().isEmpty() ? "" :
				" implements " + vc.getInterfaceClasses().map(RClass::getClassName).toString(",");

			bs("public class " + toString(vc.getTypeSig()) + impl);
			{
				vc.getProperties().forEach(p -> println(toString(p.getValueType(), true) + " " + p.getName() + ";"));
				println("");
				//***** MAIN CONSTRUCTOR
				bs("public " + vc.getTypeSig().getName().getClassName() + "(" +
					   vc.getProperties()
						   .map(p -> toString(p.getValueType().getTypeSig(), p.getValueType().isRequired() && p
							   .getDefaultValue().isPresent() == false) + " " + p.getName()).toString(", ")
					   + ")");
				{
					vc.getProperties().forEach(p -> {
						String fromValue = p.getName();
						if(p.getDefaultValue().isPresent()) {

							fromValue = p.getName() + " != null ? " + fromValue + " : " + RConstToJava
								.toJava(packageName, this::addImport, p.getDefaultValue().get());
						}
						else {
							if(p.getValueType().isRequired()) {
								addImport(Objects.class);
								if(isPrimitive(p.getValueType().getTypeSig()) == false) {
									fromValue =
										"Objects.requireNonNull(" + p.getName() + ",\"" + p.getName() + " in " + vc
											.getTypeSig().getName().getClassName() + " can\'t be null\")";
								}

							}
							else {
								if(options.generateGetters == false) {
									fromValue = "Optional.ofNullable(" + fromValue + ")";
								}
							}

						}
						println("this." + p.getName() + " = " + fromValue + ";");
					});
				}
				be();
				//****** EXTRA CONSTRUCTORS FOR NULLABLE PROPERTIES
				PList<RProperty> req = vc.getProperties().filter(this::isRequired);
				if(req.size() != vc.getProperties().size()) {
					bs("public " + vc.getTypeSig().getName().getClassName() + "(" +
						   req.map(p -> toString(p.getValueType().getTypeSig(), p.getValueType().isRequired()) + " " + p
							   .getName()).toString(", ")
						   + ")");
					{
						println("this(" + vc.getProperties().map(p -> isRequired(p) ? p.getName() : "null")
							.toString(",") + ");");
					}
					be();
				}

				//****** GETTERS AND UPDATERS
				vc.getProperties().forEach(p -> {
					if(options.generateGetters) {
						generateJavaDoc(p.getAnnotations());
						String rt = toString(p.getValueType().getTypeSig(), p.getValueType().isRequired());
						String vn = p.getName();
						if(p.getValueType().isRequired() == false) {
							addImport(Optional.class);
							rt = "Optional<" + rt + ">";
							vn = "Optional.ofNullable(" + vn + ")";
						}
						println("public " + rt + " get" + firstUpper(p.getName()) + "() { return " + vn + "; }");
					}
					if(options.generateUpdaters) {
						String s =
							"public " + toString(vc.getTypeSig()) + " with" + firstUpper(p.getName()) + "(" + toString(p.getValueType()
																														   .getTypeSig(), p
																														   .getValueType()
																														   .isRequired()) + " " + p
								.getName() + ") { return new ";
						s += vc.getTypeSig().getName().getClassName();
						if(vc.getTypeSig().getGenerics().isEmpty() == false) {
							s += "<>";
						}
						s += "(" + vc.getProperties()
							.map(param -> (param.getName().equals(p.getName()) ? "" : "this.") + param.getName())
							.toString(", ") + ")";
						s += "; }";
						println(s);
					}
					if(options.generateGetters || options.generateUpdaters) {
						println("");
					}
				});
				//******* EQUALS
				generateEqualsFunction(vc);
				//******* HASHCODE
				generateHashCodeFunction(vc);

				//******* TOSTRING
				generateToString(vc);

				//******* BUILDER

				bs("static public class Builder" + getBuilderGenerics(vc));
				{
					vc.getProperties()
						.forEach(p -> println(toString(p.getValueType(), false) + " " + p.getName() + ";"));
					println("");
					vc.getProperties().forEach(p -> {
						String gen = getBuilderGenerics(vc, PMap.<String, String>empty().put(p.getName(), "SET"));
						bs("public Builder" + gen + " set" + firstUpper(p.getName()) + "(" + toString(p.getValueType()
																										  .getTypeSig(), p
																										  .getValueType()
																										  .isRequired()) + " " + p
							.getName() + ")");
						{
							println("this." + p.getName() + " = " + p.getName() + ";");
							println("return (Builder" + getBuilderGenerics(vc, PMap.<String, String>empty()
								.put(p.getName(), "SET")) + ") this;");
						}
						be();
					});


				}
				be();
				String onlyGen = vc.getTypeSig().getGenerics().map(g -> g.getName().getClassName()).toString(",");
				onlyGen = onlyGen.isEmpty() ? "" : "<" + onlyGen + ">";
				String not = getRequiredProps(vc).map(v -> "NOT")
					.plusAll(vc.getTypeSig().getGenerics().map(g -> g.getName().getClassName())).toString(",");
				String set = getRequiredProps(vc).map(v -> "SET")
					.plusAll(vc.getTypeSig().getGenerics().map(g -> g.getName().getClassName())).toString(",");
				not = not.isEmpty() ? not : "<" + not + ">";
				set = set.isEmpty() ? set : "<" + set + ">";
				addImport(Function.class);
				String p = "Function<Builder" + not + ",Builder" + set + "> supplier";
				bs("static public " + onlyGen + " " + toString(vc.getTypeSig()) + " build(" + p + ")");
				{

					println("Builder b = supplier.apply(new Builder" + (getBuilderGenerics(vc)
						.isEmpty() ? "" : "<>") + "());");
					println("return new " + vc.getTypeSig().getName().getClassName() + "(" + vc.getProperties()
						.map(v -> "b." + v.getName()).toString(", ") + ");");
					be();
				}
			}
			be();
			return toGenJava(vc.getTypeSig().getName());
		}

		private void generateEqualsFunction(RValueClass vc) {
			println("@Override");
			bs("public boolean equals(Object o)");
			{
				println("if (this == o) return true;");
				println("if (o == null || getClass() != o.getClass()) return false;");
				println("");
				if(vc.getProperties().isEmpty() == false) {
					println(vc.getTypeSig().getName().getClassName() + " _that = (" + vc.getTypeSig().getName()
						.getClassName() + ")o;");
					println("");
				}
				vc.getProperties().forEach(p -> {
					String thisVal = p.getName();
					String thatVal = "_that." + thisVal;
					if(p.getValueType().isRequired()) {
						boolean isPrim = isPrimitive(p.getValueType().getTypeSig());
						if(isPrim) {
							if(p.getValueType().getTypeSig().getName().getClassName().equals("Float")) {
								println("if(Float.compare(" + thisVal + "," + thatVal + ") != 0) return false;");
							}
							else if(p.getValueType().getTypeSig().getName().getClassName().equals("Double")) {
								println("if(Double.compare(" + thisVal + "," + thatVal + ") != 0) return false;");
							}
							else {
								println("if(" + thisVal + " != " + thatVal + ") return false;");
							}
						}
						else {
							println("if(!" + thisVal + ".equals(" + thatVal + ")) return false;");
						}
					}
					else {
						println("if(" + thisVal + "!= null ? !" + thisVal + ".equals(" + thatVal + ") : " + thatVal + " != null) return false;");
					}
				});
				println("return true;");
			}
			be();
		}

		private void generateHashCodeFunction(RValueClass vc) {
			println("@Override");
			bs("public int hashCode()");
			{
				if(vc.getProperties().isEmpty()) {
					println("return 0;");
				}
				else {
					println("int _result;");
					vc.getProperties().headMiddleEnd().forEach(t -> {
						if(t._1 == PStream.HeadMiddleEnd.head || t._1 == PStream.HeadMiddleEnd.headAndEnd) {
							print("_result = ");
						}
						else {
							print("_result = 31 * _result + ");
						}
						assert t._2 != null;
						String value = t._2.getName();
						String hash  = value + ".hashCode()";

						if(t._2.getValueType().isRequired()) {
							switch(t._2.getValueType().getTypeSig().getName().getClassName()) {
								case "Float":
									hash = "Float.hashCode(" + value + ")";
									break;
								case "Long":
									hash = "Long.hashCode(" + value + ")";
									break;
								case "Double":
									hash = "Double.hashCode(" + value + ")";
									break;
								case "Short":
									hash = "Short.hashCode(" + value + ")";
									break;
								case "Byte":
									hash = "Byte.hashCode(" + value + ")";
									break;
								case "Boolean":
									hash = "Boolean.hashCode(" + value + ")";
									break;
								case "Integer":
									hash = "Integer.hashCode(" + value + ")";
									break;
								default:
									break;
							}

						}
						else {
							hash = "(" + value + " != null ? " + hash + ": 0)";
						}
						println(hash + ";");
					});
					println("return _result;");
				}

			}
			be();
		}

		/**
		 * Generate a toString() java method for a RValueClass
		 *
		 * @param vc The case class
		 */
		public void generateToString(RValueClass vc) {
			println("@Override");
			bs("public String toString()");
			{
				println("return \"" + vc.getTypeSig().getName().getClassName() + "<<\" +");
				indent();
				boolean first = true;

				for(RProperty p : vc.getProperties()) {
					String res = "\", ";
					if(first) {
						res = "\"";
						first = false;
					}
					res += p.getName() + "=\"" + " + " + p.getName();

					if(p.getValueType().isRequired() == false) {
						res = "(" + p.getName() + " == null ? \"\" : " + res + ")";
					}
					println(res + " +");
				}
				println("\">>\";");
				outdent();
			}
			be();
			println("");
		}

		private PList<RProperty> getRequiredProps(RValueClass vc) {
			return vc.getProperties()
				.filter(p -> p.getDefaultValue().isPresent() == false && p.getValueType().isRequired());
		}

		private String getBuilderGenerics(RValueClass vc) {
			return getBuilderGenerics(vc, PMap.empty());
		}

		@SuppressWarnings("ConstantConditions")
		private String getBuilderGenerics(RValueClass vc, PMap<String, String> namesReplace) {
			PList<String> requiredProperties = getRequiredProps(vc).zipWithIndex()
				.map(t -> {
					assert t._2 != null;
					return namesReplace.getOpt(t._2.getName()).orElse("_T" + (t._1 + 1));
				}).plist();
			if(requiredProperties.isEmpty() == false) {
				addImport(SET.class);
				addImport(NOT.class);
			}
			requiredProperties =
				requiredProperties.plusAll(vc.getTypeSig().getGenerics().map(g -> g.getName().getClassName()));
			if(requiredProperties.isEmpty()) {
				return "";
			}
			return requiredProperties.toString("<", ",", ">");
		}

		private boolean isRequired(RProperty p) {
			return p.getDefaultValue().isPresent() == false && p.getValueType().isRequired();
		}

		private String toPrimString(RTypeSig sig) {
			return toString(sig, true);
		}

		private boolean isPrimitive(RTypeSig sig) {
			return toString(sig, true).equals(toString(sig, false)) == false;
		}

		private String toString(RValueType vt, boolean isFinal) {
			String res   = "";
			String value = vt.isRequired() ? toPrimString(vt.getTypeSig()) : toString(vt.getTypeSig());
			if(vt.isRequired() == false) {
				addImport(Nullable.class);

				if(options.generateGetters == false) {
					addImport(Optional.class);
					value = "Optional<" + value + ">";
				}
				else {
					res += "@Nullable ";
				}
			}
			String access = options.generateGetters ? "private" : "public";
			access += isFinal ? " final " : " ";
			return res + access + value;

		}

		public GeneratedJava generateRemoteClass(RRemoteClass rc) {
			addImport(Remotable.class);
			println("@Remotable");
			generateJavaDoc(rc.getAnnotations());
			bs("public interface " + rc.getName().getClassName());
			{
				rc.getFunctions().forEach(f -> {
					generateJavaDoc(f.getAnnotations());
					String retType;
					addImport(CompletableFuture.class);
					if(f.getResultType().isPresent() == false) {
						retType = "Object";
					}
					else {
						retType = toString(f.getResultType().get().getTypeSig());
						if(f.getResultType().get().isRequired() == false) {
							retType = "Optional<" + retType + ">";
							addImport(Optional.class);
						}
					}
					if(f.isCached()) {
						addImport(RemoteCache.class);
						println("@RemoteCache");
					}
					println("CompletableFuture<" + retType + ">\t" + f.getName() + "(" +
								f.getParams().map(p -> toString(p.getValueType().getTypeSig()) + " " + p.getName())
									.toString(", ") + ");"
					);
				});

			}
			be();

			return toGenJava(rc.getName());
		}

		private String toString(RTypeSig sig) {
			return toString(sig, false);
		}

	}


}
