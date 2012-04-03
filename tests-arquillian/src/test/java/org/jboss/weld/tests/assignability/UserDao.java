package org.jboss.weld.tests.assignability;

/**
 *
 */
public class UserDao extends Dao<User> {

    @Override
    public String getType() {
        return "UserDao";
    }
}
