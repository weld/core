package org.jboss.weld.tests.proxy.sealed;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public sealed class MyNormalScopedBean permits MyNormalScopedSubclass {

    public String ping() {
        return MyNormalScopedBean.class.getSimpleName();
    }
}
