package org.jboss.weld.tests.proxy.ignoreinvalidmethods.inheritance;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Secure
public class ImplBean extends AbstractSuperClass2 {

    public void pong() {
    }
}
