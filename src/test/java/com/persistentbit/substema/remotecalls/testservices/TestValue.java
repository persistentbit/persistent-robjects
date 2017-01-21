package com.persistentbit.substema.remotecalls.testservices;

import com.persistentbit.core.utils.BaseValueClass;

import java.util.Objects;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/01/17
 */
public class TestValue extends BaseValueClass{

	private final int  id;
	private final Name name;

	public TestValue(int id, Name name) {
		this.id = Objects.requireNonNull(id);
		this.name = Objects.requireNonNull(name);
	}

	public int getId() {
		return id;
	}

	public Name getName() {
		return name;
	}
}
