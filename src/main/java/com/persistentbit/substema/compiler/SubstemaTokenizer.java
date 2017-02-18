package com.persistentbit.substema.compiler;

import com.persistentbit.core.result.Result;
import com.persistentbit.core.tokenizer.SimpleTokenizer;
import com.persistentbit.core.tokenizer.TokenFound;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.persistentbit.core.tokenizer.SimpleTokenizer.regExMatcher;
import static com.persistentbit.substema.compiler.SubstemaTokenType.*;

/**
 * Created by petermuys on 12/09/16.
 */
public class SubstemaTokenizer{


	public static SimpleTokenizer<SubstemaTokenType> inst = new SimpleTokenizer<SubstemaTokenType>()
		.add(regExMatcher("/\\*.*?\\*/", tComment).ignore())
		.add(regExMatcher("\\n", tNl).ignore())
		.add("<<.*?>>", tDoc)
		.add("\\(", tOpen)
		.add("\\)", tClose)
		.add("\\.", tPoint)
		.add("<", tGenStart)
		.add(">", tGenEnd)
		.add("\\,", tComma)
		.add("\\?", tQuestion)
		.add("\\:", tColon)
		.add("\\;", tSemiColon)
		.add("\\{", tBlockStart)
		.add("\\}", tBlockEnd)
		.add("\\=", tAssign)
		.add("\\-\\>", tMapMap)
		.add("\\[", tArrayStart)
		.add("\\]", tArrayEnd)
		.add("-", tMin)
		.add("\\+", tPlus)
		.add("@", tAt)
		.add("[0-9]+(\\.[0-9]*)?[LlFfDdBbSs]?", tNumber)
		.add(SimpleTokenizer.stringMatcher(tString, '\'', false))
		.add(SimpleTokenizer.stringMatcher(tString, '\"', false))
		.add(SimpleTokenizer.stringMatcher(tString, '`', true))
		.add(regExMatcher("[a-zA-Z_][a-zA-Z0-9_]*", tIdentifier).map(found -> {
			switch(found.text) {
				case "package":
                    return Result.success(new TokenFound<>(found.text, tPackage, found.ignore));
                case "from":
                    return Result.success(new TokenFound<>(found.text, tFrom, found.ignore));
                case "class":
                    return Result.success(new TokenFound<>(found.text, tClass, found.ignore));
                case "import":
                    return Result.success(new TokenFound<>(found.text, tImport, found.ignore));
                case "cached":
                    return Result.success(new TokenFound<>(found.text, tCached, found.ignore));
                case "enum":
                    return Result.success(new TokenFound<>(found.text, tEnum, found.ignore));
                case "case":
                    return Result.success(new TokenFound<>(found.text, tCase, found.ignore));
                case "remote":
                    return Result.success(new TokenFound<>(found.text, tRemote, found.ignore));
				//case "ok":
				//    return Result.success(new TokenFound<>(found.text, tOK, found.ignore));
				case "exception":
					return Result.success(new TokenFound<>(found.text, tException, found.ignore));
                case "throws":
                    return Result.success(new TokenFound<>(found.text, tThrows, found.ignore));
                case "implements":
                    return Result.success(new TokenFound<>(found.text, tImplements, found.ignore));
                case "interface":
                    return Result.success(new TokenFound<>(found.text, tInterface, found.ignore));
                case "true":
                    return Result.success(new TokenFound<>(found.text, tTrue, found.ignore));
                case "false":
                    return Result.success(new TokenFound<>(found.text, tFalse, found.ignore));
                case "null":
                    return Result.success(new TokenFound<>(found.text, tNull, found.ignore));
                case "new":
                    return Result.success(new TokenFound<>(found.text, tNew, found.ignore));
                case "annotation":
                    return Result.success(new TokenFound<>(found.text, tAnnotation, found.ignore));
                default:
                    return Result.success(found);
            }
		}))
		.add(SimpleTokenizer.regExMatcher("\\s+", tWhiteSpace).ignore());


	public static void main(String... args) {
		try{
			SimpleTokenizer<SubstemaTokenType> tokenizer = SubstemaTokenizer.inst;
			String                             txt       = new String(Files.readAllBytes(Paths.get(SubstemaTokenizer.class.getResource("/app.rod").toURI())));
            System.out.println(txt);
            tokenizer.tokenize("app.rod",txt).forEach(System.out::println);

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
