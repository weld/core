package org.jboss.weld.tests.assignability;

/**
 *
 */
public class Dao<T extends Persistent> {

    public String getType() {
        return "Dao";
    }
}
