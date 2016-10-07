package com.persistentbit.substema.compiler;

/**
 * Created by petermuys on 12/09/16.
 */
public enum SubstemaTokenType {
    tPackage, tImport, tFrom, tClass, tRemote, tCase, tGenStart, tGenEnd,tComma,tQuestion,tCached,tOpen,tClose,tColon,tSemiColon,
    tComment, tWhiteSpace, tNl,tIdentifier,tPoint,tEnum,tBlockStart, tBlockEnd,tEOF,tVoid,
    tException,tThrows,tInterface,tImplements,tArrayStart,
    tArrayEnd, tAssign, tMapMap,tTrue,tNull,tFalse,tNumber,tString,tNew,tMin,tPlus,
    tAt,tAnnotation,tDoc
}
