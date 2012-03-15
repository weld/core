package org.jboss.weld.tests.contexts.request.rmi;

import javax.ejb.Remote;

@Remote
public interface Bridge {
    String doSomething();
}
