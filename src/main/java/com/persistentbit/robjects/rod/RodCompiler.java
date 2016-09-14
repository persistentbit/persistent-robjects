package com.persistentbit.robjects.rod;

import com.persistentbit.robjects.rod.values.RService;

/**
 * Created by petermuys on 14/09/16.
 */
public interface RodCompiler {
    RService compile(String name, String code);
}
