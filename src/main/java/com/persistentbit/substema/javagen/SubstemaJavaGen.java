package com.persistentbit.substema.javagen;


import com.persistentbit.core.Nullable;
import com.persistentbit.core.OK;
import com.persistentbit.core.collections.*;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.sourcegen.SourceGen;
import com.persistentbit.core.utils.builders.NOT;
import com.persistentbit.core.utils.builders.SET;
import com.persistentbit.substema.annotations.Remotable;
import com.persistentbit.substema.annotations.RemoteCache;
import com.persistentbit.substema.compiler.SubstemaCompiler;
import com.persistentbit.substema.compiler.SubstemaException;
import com.persistentbit.substema.compiler.values.*;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * //TODO add docs and cleanup
 *
 * @since 14/09/16
 */
public final class SubstemaJavaGen{

	private final JavaGenOptions options;
	private final RSubstema substema;

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
	 * Generate all the java classes for a given substema and
	 * returns them as a list
	 *
	 * @param compiler The compiler for external definitions
	 * @param options  The java generator options
	 * @param substema The substema to generate java classes for
	 *
	 * @return The List of {@link GeneratedJava} java sources
	 */
	public static PList<Result<GeneratedJava>> generate(SubstemaCompiler compiler, JavaGenOptions options,
														RSubstema substema
	) {
		return new SubstemaJavaGen(compiler, options, substema).generateSubstema();
	}

	/**
	 * Generate java using {@link #generate(SubstemaCompiler, JavaGenOptions, RSubstema)} and
	 * then write the files to a local folder
	 *
	 * @param compiler     The compiler for external definitions
	 * @param options      The java generator options
	 * @param substema     The substema to generate java classes for
	 * @param outputFolder The root folder where the generated java will be written under
	 *
	 * @return PList with the Result of the generated files
	 */
	public static PList<Result<File>> generateAndWriteToFiles(SubstemaCompiler compiler, JavaGenOptions options,
															  RSubstema substema, File outputFolder
	) {
		return Log.function(compiler, options, substema, outputFolder).code(l ->
			generate(compiler, options, substema)
				.map(generated -> generated
					.flatMap(g -> g
						.writeToFile(outputFolder))
				)
		);
	}

	/**
	 * Main function of the java generator
	 *
	 * @return The list of {@link GeneratedJava} sources
	 */
	public PList<Result<GeneratedJava>> generateSubstema() {
		return Log.function().code(log -> {
			PList<Result<GeneratedJava>> result = PList.empty();
			if(substema.getPackageDef().getAnnotations().isEmpty() == false) {
				result = result.plus(
					new Generator().generatePackageInfo(substema.getPackageDef())
				);

			}
			Function<Result<GeneratedJava>, Result<GeneratedJava>> addLog = gj -> {
				log.add(gj);
				return gj;
			};

			result = result.plusAll(
				substema.getEnums()
						.map(e -> addLog.apply(new Generator().generateEnum(e))
						));
			result = result.plusAll(
				substema.getValueClasses()
						.map(vc -> addLog.apply(new Generator().generateValueClass(vc))));
			result = result.plusAll(
				substema.getRemoteClasses()
						.map(rc -> addLog.apply(new Generator().generateRemoteClass(rc))));
			result = result.plusAll(
				substema.getInterfaceClasses()
						.map(ic -> addLog.apply(new Generator().generateInterfaceClass(ic))));

			return result.filterNulls().plist();
		});
	}


	private class Generator extends AbstractJavaGenerator{

		private Generator() {
			super(compiler, substema.getPackageName());
		}

		public Result<GeneratedJava> generatePackageInfo(RPackage pdef) {
			return Log.function(pdef).code(l -> {
				//Create the header and add it to this SourcGen instance
				SourceGen sg = new SourceGen();
				generateJavaDoc(pdef.getAnnotations());
				sg.add(this);
				sg.println("package " + packageName + ";");

				sg.println("");
				return sg.writeToString().map(str ->
					new GeneratedJava(new RClass(packageName, "package-info"), str)
				);
			});

		}

		public Result<GeneratedJava> generateEnum(REnum e) {
			return Result.function(e).code(l -> {
				generateJavaDoc(e.getAnnotations());
				bs("public enum " + e.getName().getClassName());
				{
					println(e.getValues().toString(","));
				}
				be();
				return toGenJava(e.getName());
			});
		}


		public Result<GeneratedJava> generateInterfaceClass(RInterfaceClass ic) {
			return Result.function(ic.getName()).code(l -> {
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
													 .getClassName() + " with" + firstUpper(p
								.getName()) + "(" + toString(p.getValueType()
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

			});
		}

		/**
		 * Generate java for a Case Class (or value class)
		 *
		 * @param vc The RValueClass to generate code for
		 *
		 * @return The generated Java code
		 */
		public Result<GeneratedJava> generateValueClass(RValueClass vc) {
			return Result.function(vc.getTypeSig()).code(l -> {
				generateJavaDoc(vc.getAnnotations());


				String impl = vc.getInterfaceClasses().isEmpty() ? "" :
					" implements " + vc.getInterfaceClasses().map(RClass::getClassName).toString(",");

				bs("public class " + toString(vc.getTypeSig()) + impl);
				{
					vc.getProperties()
					  .forEach(p -> println(toString(p.getValueType(), true) + " " + p.getName() + ";"));
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
							req.map(p -> toString(p.getValueType().getTypeSig(), p.getValueType()
																				  .isRequired()) + " " + p
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
								"public " + toString(vc.getTypeSig()) + " with" + firstUpper(p
									.getName()) + "(" + toString(p.getValueType()
																  .getTypeSig(), p
									.getValueType()
									.isRequired()) + " " + p
									.getName() + ") { return new ";
							s += vc.getTypeSig().getName().getClassName();
							if(vc.getTypeSig().getGenerics().isEmpty() == false) {
								s += "<>";
							}
							s += "(" + vc.getProperties()
										 .map(param -> (param.getName().equals(p.getName()) ? "" : "this.") + param
											 .getName())
										 .toString(", ") + ")";
							s += "; }";
							println(s);
						}
						if(options.generateGetters || options.generateUpdaters) {
							println("");
						}
					});
					//******* EQUALS
					generateEquals(vc);
					//******* HASHCODE
					generateHashcode(vc);

					//******* TOSTRING
					generateToString(vc);

					//******* BUILDER

					generateValueClassBuilder(vc);
				}
				be();
				return toGenJava(vc.getTypeSig().getName());

			});
		}

		private void generateValueClassBuilder(RValueClass vc) {
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
											 .plusAll(vc.getTypeSig().getGenerics()
														.map(g -> g.getName().getClassName())).toString(",");
			String set = getRequiredProps(vc).map(v -> "SET")
											 .plusAll(vc.getTypeSig().getGenerics()
														.map(g -> g.getName().getClassName())).toString(",");
			not = not.isEmpty() ? not : "<" + not + ">";
			set = set.isEmpty() ? set : "<" + set + ">";
			addImport(Function.class);
			String p = "Function<Builder" + not + ",Builder" + set + "> supplier";
			bs("static public " + onlyGen + " " + toString(vc.getTypeSig()) + " build(" + p + ")");
			{

				println("Builder b = supplier.apply(new Builder" + (getBuilderGenerics(vc)
					.isEmpty() ? "" : "<>") + "());");
				println("return new " + vc.getTypeSig().getName().getClassName() + "(" + vc.getProperties()
																						   .map(v -> "b." + v.getName())
																						   .toString(", ") + ");");
				be();
			}
		}

		private void generateEquals(RValueClass vc) {
			println("@Override");
			bs("public boolean equals(Object o)");
			{
				println("if (this == o) return true;");
				println("if (o == null || getClass() != o.getClass()) return false;");
				println("");
				PList<RProperty> equalsProps =
					vc.getProperties()
					  .filter(p -> atUtils.hasAnnotation(p.getAnnotations(), atUtils.rclassNoEquals) == false);
				if(equalsProps.isEmpty() == false) {
					println(vc.getTypeSig().getName().getClassName() + " that = (" + vc.getTypeSig().getName()
																					   .getClassName() + ")o;");
					println("");
				}
				equalsProps.forEach(p -> {
					String thisVal = p.getName();
					String thatVal = "that." + thisVal;
					if(p.getValueType().isRequired()) {
						boolean isPrim = isPrimitive(p.getValueType().getTypeSig());
						if(isPrim) {
							if(p.getValueType().getTypeSig().getName().equals("float")) {
								println("if(Float.compare(" + thisVal + "," + thatVal + " != 0) return false;");
							}
							else if(p.getValueType().getTypeSig().getName().equals("double")) {
								println("if(Double.compare(" + thisVal + "," + thatVal + " != 0) return false;");
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

		private void generateHashcode(RValueClass vc) {
			println("@Override");
			bs("public int hashCode()");
			{
				PList<RProperty> hashCodeProps = vc.getProperties()
												   .filter(p -> atUtils.hasAnnotation(p
													   .getAnnotations(), atUtils.rclassNoEquals) == false);
				if(hashCodeProps.isEmpty()) {
					println("return 0;");
				}
				else {
					println("int _result;");

					hashCodeProps.headMiddleEnd().forEach(t -> {
						if(t._1 == PStream.HeadMiddleEnd.head || t._1 == PStream.HeadMiddleEnd.headAndEnd) {
							print("_result = ");
						}
						else {
							print("_result = 31 * _result + ");
						}
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
				PList<RProperty> props = vc.getProperties()
										   .filter(p -> atUtils
											   .hasAnnotation(p.getAnnotations(), atUtils.rclassNoToString) == false);
				for(RProperty p : props) {
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

		private String getBuilderGenerics(RValueClass vc, PMap<String, String> namesReplace) {
			PList<String> requiredProperties = getRequiredProps(vc).zipWithIndex()
																   .map(t -> namesReplace.getOpt(t._2.getName())
																						 .orElse("_T" + (t._1 + 1)))
																   .plist();
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

		private String toString(RTypeSig sig) {
			return toString(sig, false);
		}

		private String toPrimString(RTypeSig sig) {
			return toString(sig, true);
		}


		private String toString(RTypeSig sig, boolean asPrimitive) {
			return Log.function(sig, asPrimitive).code(log -> {
				String gen =
					sig.getGenerics().isEmpty() ? "" : sig.getGenerics().map(this::toString).toString("<", ",", ">");
				String pname = sig.getName().getPackageName();
				String name  = sig.getName().getClassName();

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
					case "Binary":
						name = PByteList.class.getSimpleName();
						addImport(PByteList.class);
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

					case "OK":
						name = "OK";
						addImport(OK.class);
						break;

					default:
						if(pname.isEmpty()) {
							throw new SubstemaException("Don't know interal class " + name);
						}
						addImport(new RClass(pname, name));
						break;
				}

				return name + gen;

			});
		}

		private boolean isPrimitive(RTypeSig sig) {
			return toString(sig, true).equals(toString(sig, false)) == false;
		}

		private String firstUpper(String s) {
			return s.substring(0, 1).toUpperCase() + s.substring(1);
		}

		private String toString(RValueType vt, boolean isFinal) {
			return Log.function(vt, isFinal).code(log -> {
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
			});
		}

		public Result<GeneratedJava> generateRemoteClass(RRemoteClass rc) {
			return Result.function(rc.getName()).code(l -> {
				addImport(Remotable.class);
				println("@Remotable");
				generateJavaDoc(rc.getAnnotations());
				bs("public interface " + rc.getName().getClassName());
				{
					rc.getFunctions().forEach(f -> {
						generateJavaDoc(f.getAnnotations());
						String retType;
						addImport(Result.class);
						if(f.getResultType().isPresent() == false) {
							addImport(OK.class);
							retType = "OK";
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
						println("Result<" + retType + ">\t" + f.getName() + "(" +
							f.getParams().map(p -> toString(p.getValueType().getTypeSig()) + " " + p.getName())
							 .toString(", ") + ");"
						);
					});

				}
				be();

				return toGenJava(rc.getName());

			});
		}

	}


}
