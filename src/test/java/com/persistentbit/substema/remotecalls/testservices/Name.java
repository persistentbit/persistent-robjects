package com.persistentbit.substema.remotecalls.testservices;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.utils.BaseValueClass;

import java.util.Objects;
import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/01/17
 */
public class Name extends BaseValueClass{

	private final String firstName;
	@Nullable
	private final String middleName;
	private final String lastName;

	public Name(String firstName, String middleName, String lastName) {
		this.firstName = Objects.requireNonNull(firstName);
		this.middleName = middleName;
		this.lastName = Objects.requireNonNull(lastName);
	}

	public String getFirstName() {
		return firstName;
	}

	@Nullable
	public Optional<String> getMiddleName() {
		return Optional.ofNullable(middleName);
	}

	public String getLastName() {
		return lastName;
	}
}
