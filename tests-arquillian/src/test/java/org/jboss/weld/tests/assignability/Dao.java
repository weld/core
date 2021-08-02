package org.jboss.weld.tests.assignability;

import jakarta.enterprise.context.Dependent;

/**
 *
 */
@Dependent
public class Dao<T extends Persistent> {

    public String getType() {
        return "Dao";
    }
}
