package org.jboss.weld.tests.classDefining.unimplemented.interfaces.ifaces;

import jakarta.ejb.Local;

@Local
public interface NotImplementedButDeclaredInterface extends LocalInterface1 {
    String ping3();
}
