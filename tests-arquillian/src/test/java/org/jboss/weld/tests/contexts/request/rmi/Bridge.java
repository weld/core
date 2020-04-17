package org.jboss.weld.tests.contexts.request.rmi;

import jakarta.ejb.Remote;

@Remote
public interface Bridge {
    String doSomething();
}
