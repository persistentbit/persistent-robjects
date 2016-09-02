package com.persistentbit.robjects.testapi;

import com.persistentbit.core.collections.PList;
import com.persistentbit.robjects.annotations.Remotable;

import java.util.Optional;

/**
 * @author Peter Muys
 * @since 2/09/2016
 */
@Remotable
public interface UsersService {
    PList<UserData> getAllUsers();
    Optional<Integer> testOptional(Integer value);
}
