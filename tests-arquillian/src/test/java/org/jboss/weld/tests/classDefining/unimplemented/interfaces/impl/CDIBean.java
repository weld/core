package org.jboss.weld.tests.classDefining.unimplemented.interfaces.impl;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.weld.tests.classDefining.unimplemented.interfaces.ifaces.BeanIface;
import org.jboss.weld.tests.classDefining.unimplemented.interfaces.ifaces.NotImplementedIface;

// Plain CDI bean which doesn't implement one interface but has its method
// A CDI extension attempts to add this type programatically
@ApplicationScoped
public class CDIBean implements BeanIface {
    @Override
    public String ping1() {
        return BeanIface.class.getSimpleName();
    }

    public String ping3() {
        return NotImplementedIface.class.getSimpleName();
    }
}
