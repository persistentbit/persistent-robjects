package com.persistentbit.substema.codegen;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.printing.LogFormatter;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;

/**
 * TODOC
 *
 * @author petermuys
 * @since 12/01/17
 */
public class SubstemaTestUtils{

	public static LogFormatter testLogFormatter = ModuleCore.createLogFormatter(true);
	public static LogPrint     testLogPrint     = LogPrintStream.sysOut(testLogFormatter);
}
