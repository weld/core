package org.jboss.weld.tests.classDefining.unimplemented.interfaces.impl;

import jakarta.ejb.Local;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

import org.jboss.weld.tests.classDefining.unimplemented.interfaces.ifaces.LocalInterface1;
import org.jboss.weld.tests.classDefining.unimplemented.interfaces.ifaces.LocalInterface2;
import org.jboss.weld.tests.classDefining.unimplemented.interfaces.ifaces.NotImplementedButDeclaredInterface;

// NOTE: the bean intentionally declares NotImplementedButDeclaredInterface but does *not* implement it directly
@Stateless
@LocalBean
@Local({ LocalInterface1.class, LocalInterface2.class,
        NotImplementedButDeclaredInterface.class })
public class StatelessLocalBean implements LocalInterface1, LocalInterface2 {

    @Override
    public String ping1() {
        return LocalInterface1.class.getSimpleName();
    }

    @Override
    public String ping2() {
        return LocalInterface2.class.getSimpleName();
    }

    public String ping3() {
        return NotImplementedButDeclaredInterface.class.getSimpleName();
    }
}
