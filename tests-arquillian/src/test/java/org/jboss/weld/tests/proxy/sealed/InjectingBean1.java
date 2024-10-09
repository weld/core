package org.jboss.weld.tests.proxy.sealed;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

// creates an injection point as CDI specification enforces proxyability only when there is an IP present
@ApplicationScoped
public class InjectingBean1 {

    @Inject
    MyNormalScopedBean bean;
}
