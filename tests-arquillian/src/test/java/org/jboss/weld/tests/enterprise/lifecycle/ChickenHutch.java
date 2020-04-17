package org.jboss.weld.tests.enterprise.lifecycle;

import jakarta.ejb.Local;

@Local
public interface ChickenHutch {

    void ping();

}
