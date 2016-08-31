package com.persistentbit.robjects.describe;

import com.persistentbit.jjson.mapping.JJMapper;

/**
 * @author Peter Muys
 * @since 31/08/2016
 */
public class RemoteDescriber {
    private final JJMapper  mapper;

    private RemoteDescriber(JJMapper mapper) {
        this.mapper = mapper;
    }

}
