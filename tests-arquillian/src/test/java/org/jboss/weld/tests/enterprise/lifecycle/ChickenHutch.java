package org.jboss.weld.tests.enterprise.lifecycle;

import javax.ejb.Local;

@Local
public interface ChickenHutch {

    void ping();

}
