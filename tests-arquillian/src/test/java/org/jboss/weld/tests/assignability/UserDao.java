package org.jboss.weld.tests.assignability;

import jakarta.enterprise.context.Dependent;

/**
 *
 */
@Dependent
public class UserDao extends Dao<User> {

    @Override
    public String getType() {
        return "UserDao";
    }
}
