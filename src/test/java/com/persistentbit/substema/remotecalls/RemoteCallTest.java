package com.persistentbit.substema.remotecalls;

import com.persistentbit.core.testing.TestCase;
import com.persistentbit.substema.SubstemaTestUtils;

/**
 * TODOC
 *
 * @author petermuys
 * @since 20/01/17
 */
public class RemoteCallTest extends SubstemaTestUtils{

	static final TestCase callCached = TestCase.name("Call Cached Remote values").code(tr -> {

	});

	public static void main(String[] args) {
		new RemoteCallTest().testAll();
	}
}
