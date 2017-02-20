package com.persistentbit.substema.experiments;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.OK;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.parser.Scan;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;
import com.persistentbit.substema.compiler.values.*;
import com.persistentbit.substema.compiler.values.expr.RConst;

/**
 * TODOC
 *
 * @author petermuys
 * @since 19/02/17
 */
public class SubstemaParser{

	public static final Parser<String> ws = Scan.whiteSpace;

	public static final Parser<String> identifier =
		ws.skipAndThen(Scan.identifier
				.verify("An Identifier can't start with '_'", id -> id.startsWith("_") == false)
				.skip(ws));

	public static final Parser<String> term(String term) {
		return ws
			.skipAndThen(Scan.term(term))
			.andThenSkip(ws);
	}

	public static final Parser<String> parsePackageName =
		Parser.oneOrMoreSep(identifier, term("."))
			  .map(list -> list.toString("."))
			  .onErrorAddMessage("package name expected");

	public static final Parser<RImport> parseImport =
		term("import")
			.skipAndThen(parsePackageName)
			.map(packageName -> new RImport(packageName))
			.skip(ws)
			.onErrorAddMessage("Import statement expected");

	public static final <R> Parser<R> block(Parser<R> content) {
		return
			term("{")
				.skipAndThen(content)
				.andThenSkip(term("}"))
			;
	}

	public static final Parser<RValueClass> parseValueClass = Parser.toDo("value class");

	public static final Parser<RRemoteClass> parseRemoteClass = Parser.toDo("remote class");

	public static final Parser<REnum> parseEnum = Parser.toDo("enum");

	public static final Parser<RAnnotationDef> parseAnnotationDef = Parser.toDo("Annotation definition");


	public static final Parser<RClass> parseClassName =
		Parser.oneOrMoreSep(identifier, term("."))
			  .map(ids -> {
				  if(ids.size() == 1) {
					  return new RClass("", ids.get(0));
				  }
				  return new RClass(ids.dropLast().toString("."), ids.lastOpt().get());
			  })
			  .onErrorAddMessage("Class name expected");


	public static final Parser<RTypeSig> parseTypeSig() {
		return source ->
			identifier
				.andThen(
					term("<")
						.skipAndThen(Parser.oneOrMoreSep(parseTypeSig(), term(",")))
						.andThenSkip(term(">"))
						.optional().map(optList -> optList.orElse(PList.empty()))
				).map(nameAndOptGenerics ->
				new RTypeSig(new RClass(nameAndOptGenerics._1), nameAndOptGenerics._2)
			)
				.skip(ws)
				.onErrorAddMessage("Type signature expected.")
				.parse(source);
	}

	public static final Parser<RValueType> parseValueType =
		term("?").optional()
				 .andThen(parseTypeSig())
				 .map(requiredAndTypeSig -> new RValueType(requiredAndTypeSig._2, requiredAndTypeSig._1
					 .isPresent() == false))
				 .skip(ws)
				 .onErrorAddMessage("Expected value type");

	public static final Parser<RConst> parseConst = Parser.toDo("RConst");

	public static final Parser<RAnnotation> parseAnnotation = Parser.toDo("Annotation");

	public static <R extends Annotated> Parser<R> parseAnnotated(Parser<R> parser) {
		return source -> {
			ParseResult<PList<RAnnotation>> resAtList = Parser.zeroOrMore(parseAnnotation).parse(source);
			if(resAtList.isFailure()) {
				return resAtList.map(v -> null);
			}
			source = resAtList.getSource();
			ParseResult<R> res = parser.parse(source);
			if(res.isSuccess()) {
				res = res.map(r -> (R) r.withAnnotations(resAtList.getValue()));
			}
			return res;
		};
	}

	public static final Parser<RProperty> parserRProperty =
		parseAnnotated(identifier
			.andThenSkip(term(":"))
			.andThen(parseValueType)
			.andThen(
				term("=")
					.skipAndThen(parseConst)
					.optional()
			)
			.map(idValTypeOptConst ->
				new RProperty(
					idValTypeOptConst._1._1,
					idValTypeOptConst._1._2,
					idValTypeOptConst._2.orElse(null)
				)
			)
			.skip(ws)
			.onErrorAddMessage("Expected a property declaration")
		);

	public static final Parser<RInterfaceClass> parseInterface =
		parseAnnotated(
			term("interface")
				.skipAndThen(parseClassName)
				.andThen(block(Parser.zeroOrMore(parserRProperty.andThenSkip(term(";")))))
				.skip(ws)
				.onErrorAddMessage("Interface definition expected")
				.map(t -> new RInterfaceClass(t._1, t._2, PList.empty()))
		);
	//-------------------------- TESTING ---------------------


	static final TestCase testInterface = TestCase.name("parse Interface def").code(tr -> {
		Fail                   fail = Fail.of(tr, parseInterface);
		ValEq<RInterfaceClass> eq   = ValEq.of(tr, parseInterface);
		fail.test("");
		fail.test("interface");
		fail.test("interface 100");
		fail.test("?");
		eq.test("interface test{}", new RInterfaceClass(new RClass("", "test"), PList.empty(), PList.empty()));
		eq.test("interface test{ name : String;}", new RInterfaceClass(new RClass("", "test"), PList.val(new RProperty(
			"name", new RValueType(new RTypeSig(new RClass("String")), true), (RConst) null)), PList.empty()));
		eq.test("interface test{ name : String; name2:?Integer;}", new RInterfaceClass(new RClass("", "test"), PList
			.val(new RProperty(
				"name", new RValueType(new RTypeSig(new RClass("String")), true), (RConst) null), new RProperty(
				"name2", new RValueType(new RTypeSig(new RClass("Integer")), false), (RConst) null)), PList.empty()));
	});


	static final TestCase testProperty = TestCase.name("parse property").code(tr -> {
		Fail fail = Fail.of(tr, parserRProperty);
		fail.test("");
		fail.test("/");
		fail.test("name");
		fail.test("name:");
		fail.test("name:?");
		ValEq<RProperty> eq = ValEq.of(tr, parserRProperty);
		eq.test("name : String", new RProperty(
				"name",
				new RValueType(new RTypeSig(new RClass("String")), true),
				(RConst) null
			)
		);
		eq.test("name2 : ?String", new RProperty(
				"name2",
				new RValueType(new RTypeSig(new RClass("String")), false),
				(RConst) null
			)
		);
	});

	static final TestCase testValueType = TestCase.name("parse value type").code(tr -> {
		Fail fail = Fail.of(tr, parseValueType);
		fail.test("");
		fail.test("/");
		fail.test("?100");
		fail.test("?");
		ValEq<RValueType> eq = ValEq.of(tr, parseValueType);
		eq.test("?name/bla", new RValueType(new RTypeSig(new RClass("name")), false));
		eq.test("name/bla", new RValueType(new RTypeSig(new RClass("name")), true));
	});

	static final TestCase testImport = TestCase.name("parse import").code(tr -> {
		Fail fail = Fail.of(tr, parseImport);
		fail.test("");
		fail.test("import");
		fail.test("import 2");
		ValEq<RImport> eq = ValEq.of(tr, parseImport);
		eq.test("import a", new RImport("a"));
		eq.test("import a . b .c/bla", new RImport("a.b.c"));
	});

	static final TestCase testTypeSig = TestCase.name("parse type sig").code(tr -> {
		tr.isFalse(doParse("", parseTypeSig()).isSuccess());
		tr.isFalse(doParse("1", parseTypeSig()).isSuccess());
		ValEq<RTypeSig> eq = ValEq.of(tr, parseTypeSig());
		eq.test("NoTypes/2", new RTypeSig(new RClass("NoTypes")));
		eq.test("NoTypes/2", new RTypeSig(new RClass("NoTypes")));
		eq.test("name<>", new RTypeSig(new RClass("name")));
		eq.test("name < gen1 > ", new RTypeSig(new RClass("name"), PList
			.val(new RTypeSig(new RClass("gen1")))));
		eq.test("name< gen1 , gen2 >", new RTypeSig(new RClass("name"), PList
			.val(new RTypeSig(new RClass("gen1")), new RTypeSig(new RClass("gen2")))));
		eq.test("name<gen1<gen2>>", new RTypeSig(new RClass("name"), PList
			.val(new RTypeSig(new RClass("gen1"), PList.val(new RTypeSig(new RClass("gen2")))))));

	});


	static final TestCase testIdentifier = TestCase.name("parse identifier").code(tr -> {
		tr.isFalse(doParse("", identifier).isSuccess());
		tr.isFalse(doParse("_", identifier).isSuccess());
		tr.isFalse(doParse("_ab", identifier).isSuccess());
		tr.isEquals(doParse("Hello_Peter_007,other", identifier).getValue(), "Hello_Peter_007");
	});

	static final TestCase testParseClassName = TestCase.name("parse RClass").code(tr -> {
		tr.isFalse(doParse("", parseClassName).isSuccess());
		tr.isEquals(doParse("className", parseClassName).getValue(), new RClass("", "className"));
		tr.isEquals(doParse("a.className", parseClassName).getValue(), new RClass("a", "className"));
		tr.isEquals(doParse("a.bb.cc.className", parseClassName).getValue(), new RClass("a.bb.cc", "className"));
	});

	static private <R> ParseResult<R> doParse(String source, Parser<R> parser) {
		return parser.parse(Source.asSource("test", source));
	}

	static private final <T> void valueEquals(TestRunner tr, String source, Parser<T> parser, T value) {
		tr.isEquals(doParse(source, parser).getValue(), value);
	}

	@FunctionalInterface
	interface ValEq<T>{

		OK test(String source, T value);

		static <R> ValEq<R> of(TestRunner tr, Parser<R> parser) {
			return (source, value) -> {
				tr.isEquals(doParse(source, parser).getValue(), value);
				return OK.inst;
			};
		}
	}

	@FunctionalInterface
	interface Fail{

		OK test(String source);

		static Fail of(TestRunner tr, Parser<?> parser) {
			return (source) -> {
				tr.isFalse(doParse(source, parser).isSuccess());
				return OK.inst;
			};
		}
	}

	public static void main(String[] args) {
		LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();
		TestRunner.runAndPrint(lp, SubstemaParser.class);
	}
}
