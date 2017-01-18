package com.persistentbit.substema;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.printing.LogFormatter;

/**
 * TODOC
 *
 * @author petermuys
 * @since 12/01/17
 */
public final class ModuleSubstema{

	public static LogFormatter createLogPrinter(boolean inColor) {
		return ModuleCore.createLogFormatter(inColor);
	}
}
