package org.jboss.weld.tests.classDefining.unimplemented.interfaces;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;

import org.jboss.weld.tests.classDefining.unimplemented.interfaces.ifaces.NotImplementedIface;
import org.jboss.weld.tests.classDefining.unimplemented.interfaces.impl.CDIBean;

public class MyExtension implements Extension {

    public void pba(@Observes ProcessBeanAttributes<CDIBean> pba) {
        // add the type of the interface the class doesn't directly implement
        pba.configureBeanAttributes().addType(NotImplementedIface.class);
    }
}
