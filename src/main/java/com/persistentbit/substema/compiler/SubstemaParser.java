package com.persistentbit.substema.compiler;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.POrderedMap;
import com.persistentbit.core.function.Function2;
import com.persistentbit.core.tokenizers.Token;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.UString;
import com.persistentbit.substema.compiler.values.*;
import com.persistentbit.substema.compiler.values.expr.*;

import java.util.Iterator;
import java.util.function.Supplier;

import static com.persistentbit.substema.compiler.SubstemaTokenType.*;

/**
 * @author Peter Muys
 * @since 12/09/16
 */
public class SubstemaParser{

	private final String                                    packageName;
	private Iterator<Token<SubstemaTokenType>> tokens;
	private       Token<SubstemaTokenType>                  current;
	private       SubstemaTokenType                  currentType;
	private		  String currentText;

	public SubstemaParser(String packageName, Iterator<Token<SubstemaTokenType>> tokens) {
		this.packageName = packageName;
		this.tokens = tokens;
		next();
	}

	private Token<SubstemaTokenType> next() {
		if(tokens.hasNext() == false){
			throw new SubstemaParserException(current.pos, "Unexpected End-Of-File");
		}

		current = tokens.next();
		if(current.result.leftOpt().isPresent()){
			throw new SubstemaParserException(current.pos, current.result.leftOpt().get());
		}
		Token.Data<SubstemaTokenType> data = current.result.rightOpt().get();
		currentText = data.text;
		currentType = data.type;
		return current;
	}

	/**
	 * Main function to parse a Substema File.<br>
	 *
	 * @return The parsed RSubstema.
	 */
	public RSubstema parseSubstema() {
		RPackage               packageDef     = null;
		PList<RImport>         imports        = PList.empty();
		PList<RValueClass>     values         = PList.empty();
		PList<RRemoteClass>    remotes        = PList.empty();
		PList<REnum>           enums          = PList.empty();
		PList<RInterfaceClass> interfaces     = PList.empty();
		PList<RAnnotationDef>  annotationDefs = PList.empty();
		while(currentType != tEOF) {
			PList<RAnnotation> annotations = parseAnnotations();
			switch(currentType) {
				case tPackage:
					if(packageDef != null) {
						throw new SubstemaParserException(current.pos, "There can be only one package definition");
					}
					packageDef = new RPackage(annotations);
					next();//skip package
					skipEndOfStatement();
					break;
				case tImport:
					if(annotations.isEmpty() == false) {
						throw new SubstemaParserException(current.pos, "Did not expect annotations for an import statement");
					}
					imports = imports.plus(parseImport());
					break;
				case tCase:
					values = values.plus(parseValueClass(annotations));
					break;
				case tRemote:
					remotes = remotes.plus(parseRemoteClass(annotations));
					break;
				case tEnum:
					enums = enums.plus(parseEnum(annotations));
					break;
				case tInterface:
					interfaces = interfaces.plus(parseInterface(annotations));
					break;
				case tAnnotation:
					if(annotations.isEmpty() == false) {
						throw new SubstemaParserException(current.pos, "Dit not expect annotations for an annotation definition");
					}
					annotationDefs = annotationDefs.plus(parseAnnotationDef(annotations));
					break;
				default:
					throw new SubstemaParserException(current.pos, "Expected a definition, not '" + currentText + "'");
			}
		}
		if(packageDef == null) {
			packageDef = new RPackage(PList.empty());
		}

		return new RSubstema(packageDef, imports, packageName, enums, values, remotes, interfaces, annotationDefs);
	}

	/**
	 * Parse an annotation definition.<br>
	 * Example: <br>
	 * {@code
	 * annotation AnnotationName{
	 * property1:Type = default1;
	 * property2:Type;
	 * property3:?Type;
	 * }
	 * <p>
	 * }
	 *
	 * @return The Annotation definition
	 */
	private RAnnotationDef parseAnnotationDef(PList<RAnnotation> annotations) {
		skip(tAnnotation, "'annotation' keyword expected");
		RClass           cls   = parseRClass(packageName);
		PList<RProperty> props = PList.empty();
		if(currentType == tBlockStart) {
			next();
			while(currentType != tEOF && currentType != tBlockEnd) {
				props = props.plus(parseRProperty(parseAnnotations()));
			}
			skip(tBlockEnd, "'}' expected for the end of the annotation definition of " + cls.getClassName());
		}
		return new RAnnotationDef(cls, props, annotations);
	}

	/**
	 * Try to parse a list of annotations values at the current position.<br>
	 * If none are found, then an empty PList is returned.<br>
	 * if a doc token <<....>> is found than this is transformed to a @Doc annotation<br>
	 *
	 * @return The List of parsed RAnnotations
	 */
	private PList<RAnnotation> parseAnnotations() {
		PList<RAnnotation> result = PList.empty();
		while(currentType == tAt || currentType == tDoc) {
			if(currentType == tDoc) {
				result = result.plus(
					new RAnnotation(
						SubstemaUtils.docRClass,
						PMap.<String, RConst>empty().put(
							"info",
							new RConstString(
								UString.escapeToJavaString(
									currentText.substring(2, currentText.length() - 2)
								)

							)
						)
					)
				);
				next();//Skip doc token
			}
			else {
				next();//skip @
				RClass               name   = parseRClass("");
				PMap<String, RConst> values = PMap.empty();

				if(currentType == tOpen) {
					next();
					if(currentType != tClose) {
						values = values.plusAll(sep(tComma, this::parseMaybeNamedConst));
					}

					skip(tClose, "')' expected to close the annotation " + name);
				}
				result = result.plus(new RAnnotation(name, values));
			}


		}
		return result;
	}

	/**
	 * Parse an Import statement
	 */
	private RImport parseImport() {
		skip(tImport, "'import' expected.");
		String packageName = parsePackageName();
		skip(tSemiColon, "';' expected after import package name");
		return new RImport(packageName);
	}

	private RTypeSig parseTypeSignature() {
		assertType(tIdentifier, "Class name expected.");
		String className = currentText;

		next(); //skip class name
		PList<RTypeSig> generics = PList.empty();
		if(currentType == tGenStart) {
			next(); //skip <
			while(currentType != tEOF) {
				generics = generics.plus(parseTypeSignature());
				if(currentType == tGenEnd) {
					next(); //skip >
					break;
				}
				skip(tComma, "Expected ',' ");
			}
		}
		return new RTypeSig(new RClass("", className), generics);
	}

	private void assertType(SubstemaTokenType type, String msg) {
		if(currentType != type) {
			throw new SubstemaParserException(current.pos, msg);
		}
	}

	private void skip(SubstemaTokenType type, String msg) {
		assertType(type, msg);
		next();
	}
	private void error(String msg){
		throw new SubstemaParserException(current.pos,msg);
	}


	/**
	 * parse the Class name and create a RClass with the given packageName and the parsed class Name
	 *
	 * @param packageName The packageName for the RClass
	 *
	 * @return a new RClass
	 */
	private RClass parseRClass(String packageName) {
		String name = currentText;
		skip(tIdentifier, "identifier expected!");
		return new RClass(packageName, name);
	}

	private RInterfaceClass parseInterface(PList<RAnnotation> annotations) {
		skip(tInterface, "'interface' expected");
		RClass           name = parseRClass(packageName);
		PList<RProperty> p    = PList.empty();
		if(currentType == tBlockStart) {
			next();
			while(currentType != tEOF && currentType != tBlockEnd) {
				p = p.plus(parseRProperty(parseAnnotations()));
			}
			skip(tBlockEnd, "'}' expected");
		}
		return new RInterfaceClass(name, p, annotations);
	}

	/**
	 * Parse a case class definition.
	 *
	 * @param annotations The List of annotations for the value class
	 *
	 * @return The parse value class
	 */
	private RValueClass parseValueClass(PList<RAnnotation> annotations) {

		skip(tCase, "'value' expected");
		skip(tClass, "'class' expected");
		RTypeSig      sig        = parseTypeSignature();
		PList<RClass> interfaces = PList.empty();
		if(currentType == tImplements) {
			next();//skip implements;
			interfaces = sep(tComma, () -> parseRClass(""));
		}


		PList<RProperty> props = PList.empty();
		if(currentType == tBlockStart) {
			next();
			while(currentType != tEOF && currentType != tBlockEnd) {
				props = props.plus(parseRProperty(parseAnnotations()));
			}
			skip(tBlockEnd, "'}' expected");
		}
		return new RValueClass(sig, props, interfaces, annotations);
	}

	private RValueType parseRValueType() {
		boolean required = true;
		if(currentType == tQuestion) {
			required = false;
			next();
		}
		RTypeSig sig = parseTypeSignature();
		return new RValueType(sig, required);
	}

	private RProperty parseRProperty(PList<RAnnotation> annotations) {
		assertType(tIdentifier, "property name expected");
		String name = currentText;
		next(); //skip name;

		skip(tColon, "':' expected after property name");
		RValueType valueType    = parseRValueType();
		RConst     defaultValue = null;
		if(currentType == tAssign) {
			next();//skip '='
			defaultValue = parseConst();
		}
		skipEndOfStatement();
		return new RProperty(name, valueType, defaultValue, annotations);
	}

	/**
	 * Parse a value literal.<br>
	 *
	 * @return The literal.
	 */
	private RConst parseConst() {
		Tuple2<String,RConst> res = parseMaybeNamedConst();
		if(res._1 != null){
			error("Didn't expect a named constant");
		}
		return res._2;
	}
	private Tuple2<String,RConst> parseMaybeNamedConst() {
		switch(currentType) {
			case tArrayStart:
				return Tuple2.of(null,parseValueArray());
			case tTrue:
			case tFalse:
				return Tuple2.of(null,parseValueBoolean());
			case tMin:
			case tPlus:
			case tNumber:
				return Tuple2.of(null,parseValueNumber());
			case tNew:
				return Tuple2.of(null,parseValueValueObject());
			case tIdentifier:
				return parserValueEnumOrNamedConstant();
			case tNull:
				next();
				return Tuple2.of(null,RConstNull.Null);
			case tString: {
				String value = currentText;
				next();
				return Tuple2.of(null,new RConstString(value.substring(1, value.length() - 1)));
			}
			default:
				throw new SubstemaParserException(current.pos, "Expected a literal value");
		}
	}

	private RConstValueObject parseValueValueObject() {
		skip(tNew, "'new' expected");
		RClass name = parseRClass("");
		skip(tOpen, "'(' expected after value class name");
		POrderedMap<String, RConst> args = POrderedMap.empty();
		if(currentType != tClose) {
			args = args.plusAll(sep(tComma, () -> {
				String propName = currentText;
				skip(tIdentifier, "Expected property name.");
				skip(tColon, "':' expected after property name.");
				return new Tuple2<>(propName, parseConst());
			}));
		}
		skip(tClose, "')' expected after value class arguments");
		return new RConstValueObject(new RTypeSig(name), args);
	}

	private Tuple2<String,RConst> parserValueEnumOrNamedConstant() {
		RClass cls = parseRClass("");
		if(currentType == tAssign){
			next(); //skip =
			//We have a named constant.
			String name = cls.getClassName();
			if(cls.getPackageName().isEmpty() == false) {
				error("Expected a property name");
			}
			return Tuple2.of(name, parseConst());
		}
		skip(tPoint, "'.' expected after enum name");
		String valueName = currentText;
		skip(tIdentifier, "enum value name expected");
		return Tuple2.of(null,new RConstEnum(cls, valueName));
	}

	private RConstNumber parseValueNumber() {
		boolean negative = false;
		if(currentType == tPlus) {
			next();
		}
		else if(currentType == tMin) {
			negative = true;
			next();
		}
		if(currentType != tNumber) {
			throw new SubstemaParserException(current.pos, "Expected a number");
		}
		RClass cls = SubstemaUtils.integerRClass;
		String txt = (negative ? "-" : "") + currentText.toLowerCase();
		Function2<Long, Long, Boolean> check = (min, max) -> {
			long value = Long.parseLong(txt);
			return min <= value && value <= max;
		};
		Number value;
		if(txt.endsWith("l")) {
			cls = SubstemaUtils.longRClass;
			value = Long.parseLong(UString.dropLast(txt, 1));
		}
		else if(txt.contains("f")) {
			cls = SubstemaUtils.floatRClass;
			value = Float.parseFloat(UString.dropLast(txt, 1));
		}
		else if(txt.contains("s")) {
			cls = SubstemaUtils.shortRClass;
			if(check.apply((long) Short.MIN_VALUE, (long) Short.MAX_VALUE) == false) {
				throw new SubstemaParserException(current.pos, "Value " + txt + " is to big or to small to be a Short");
			}
			value = Short.parseShort(UString.dropLast(txt, 1));
		}
		else if(txt.contains("b")) {
			cls = SubstemaUtils.byteRClass;
			if(check.apply((long) Byte.MIN_VALUE, (long) Byte.MAX_VALUE) == false) {
				throw new SubstemaParserException(current.pos, "Value " + txt + " is to big or to small to be a Byte");
			}
			value = Byte.parseByte(UString.dropLast(txt, 1));
		}
		else if(txt.contains("d") || txt.contains(".")) {
			cls = SubstemaUtils.doubleRClass;
			value = Double.parseDouble(UString.dropLast(txt, 1));
		}
		else {
			if(check.apply((long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE) == false) {
				cls = SubstemaUtils.longRClass;
				value = Long.parseLong(txt);
			}
			else {
				value = Integer.parseInt(txt);
			}

		}
		next(); //skip number
		return new RConstNumber(cls, value);
	}



	private RConstArray parseValueArray() {
		skip(tArrayStart, "'[' expected");
		PList<RConst> elements = PList.empty();
		if(currentType != tArrayEnd) {
			elements = sep(tComma, this::parseConst);
		}
		skip(tArrayEnd, "']' expected");
		return new RConstArray(elements);
	}

	private RConstBoolean parseValueBoolean() {
		if(currentType == tTrue) {
			next();
			return new RConstBoolean(true);
		}
		if(currentType == tFalse) {
			next();
			return new RConstBoolean(false);
		}
		throw new SubstemaParserException(current.pos, "Expected a boolean value");
	}

	private RFunctionParam parseFunctionParam(PList<RAnnotation> annotations) {
		assertType(tIdentifier, "parameter name expected");
		String name = currentText;
		next(); //skip name;

		skip(tColon, "':' expected after parameter name");
		RValueType valueType = parseRValueType();

		return new RFunctionParam(name, valueType, annotations);
	}

	private RRemoteClass parseRemoteClass(PList<RAnnotation> annotations) {
		skip(tRemote, "'remote' expected");
		skip(tClass, "'class' expected");
		assertType(tIdentifier, "function name expected");
		RClass name = new RClass(packageName, currentText);
		next(); //skip name;

		PList<RFunction> functions = PList.empty();
		if(currentType == tBlockStart) {
			next();
			while(currentType != tEOF && currentType != tBlockEnd) {
				functions = functions.plus(parseRFunction(parseAnnotations()));
			}
			skip(tBlockEnd, "'}' expected");
		}
		return new RRemoteClass(name, functions, annotations);
	}

	private RFunction parseRFunction(PList<RAnnotation> annotations) {
		assertType(tIdentifier, "function name expected");
		String name = currentText;
		next(); //skip name;
		skip(tOpen, "'(' expected after function name");
		PList<RFunctionParam> params = PList.empty();
		if(currentType == tIdentifier) {
			params = sep(tComma, () -> parseFunctionParam(parseAnnotations()));
		}
		skip(tClose, "')' expected after function parameters");
		skip(tColon, "':' expected to define the function return type");
		RValueType returnType = null;
		boolean    cached     = false;
		if(currentType != tOK) {
			returnType = parseRValueType();
			if(currentType == tCached) {
				if(params.isEmpty() == false) {
					throw new SubstemaParserException(current.pos, "cached result is not supported on functions with parameters.");
				}
				cached = true;
				next(); //skip cached
			}
		}
		else {
			next();//skip void
		}

		skipEndOfStatement();
		return new RFunction(name, params, returnType, cached, annotations);
	}

	private String parsePackageName() {
		//skip(tPackage,"package expected");

		return sep(tPoint, () -> {
			assertType(tIdentifier, "name expected");
			String name = currentText;
			next();
			return name;
		}).toString(".");
	}

	private void skipEndOfStatement() {
		skip(tSemiColon, "';' expected after statement.");
	}

	/**
	 * Parse a list of values, separated by the supplied separation token type.<br>
	 *
	 * @param sep The token type of the separator
	 * @param r   The parser for 1 value
	 * @param <T> The type of the value parsers.
	 *
	 * @return the list of parsed items.
	 */
	private <T> PList<T> sep(SubstemaTokenType sep, Supplier<T> r) {
		PList<T> res = PList.empty();
		while(currentType != tEOF) {
			res = res.plus(r.get());
			if(currentType != sep) {
				return res;
			}
			next();
		}
		return res;
	}

	private REnum parseEnum(PList<RAnnotation> annotations) {
		skip(tEnum, "'enum' expected");
		assertType(tIdentifier, "enum name expected.");
		String name = currentText;
		next();
		skip(tBlockStart, "'{' expected for enum definition");
		PList<String> values = PList.empty();
		if(currentType != tSemiColon) {
			values = sep(tComma, () -> {
				PList<RAnnotation> valueAnnotations = parseAnnotations();
				//TODO Annotations on values should be added to the result REnum values
				assertType(tIdentifier, "enum value name expected");
				String valueName = currentText;
				next();
				return valueName;
			});
		}
		next();//skip ;
		skip(tBlockEnd, "'}' expected to end enum definition for '" + name + "'");
		return new REnum(new RClass(packageName, name), values, annotations);
	}

	private RTypeSig replaceType(RTypeSig type, RClass genericName, RTypeSig genericType) {
		if(type.getName().equals(genericName)) {
			return genericType;
		}
		PList<RTypeSig> newGen = type.getGenerics().map(gt -> replaceType(gt, genericName, genericType));
		return new RTypeSig(type.getName(), newGen);
	}
}
