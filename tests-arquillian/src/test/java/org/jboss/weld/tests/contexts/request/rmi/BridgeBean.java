package org.jboss.weld.tests.contexts.request.rmi;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class BridgeBean implements Bridge {
    @Inject @My private Config config;

    public String doSomething() {
        System.out.println("Bridge.doSomething.");
        return config.toString();
    }
}
