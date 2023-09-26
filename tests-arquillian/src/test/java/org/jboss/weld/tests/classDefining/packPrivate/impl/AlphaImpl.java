package org.jboss.weld.tests.classDefining.packPrivate.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;

import org.jboss.weld.tests.classDefining.packPrivate.api.Alpha;
import org.jboss.weld.tests.classDefining.packPrivate.interceptor.SomeBinding;

/**
 * Class is intentionally package private, @Typed to just the interface type and normal scoped to enforce client proxy
 */
@ApplicationScoped
@SomeBinding
@Typed(Alpha.class)
class AlphaImpl implements Alpha {
    @Override
    public String ping() {
        return Alpha.class.getSimpleName();
    }
}
