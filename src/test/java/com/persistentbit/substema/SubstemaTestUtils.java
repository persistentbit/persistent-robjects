package com.persistentbit.substema;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.printing.LogFormatter;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.testing.TestRunner;

/**
 * TODOC
 *
 * @author petermuys
 * @since 12/01/17
 */
public class SubstemaTestUtils{

	public static LogFormatter testLogFormatter = ModuleCore.createLogFormatter(true);
	public static LogPrint     testLogPrint     = LogPrintStream.sysOut(testLogFormatter);

	public void testAll() {
		TestRunner.runAndPrint(testLogPrint, this.getClass());
	}
}
