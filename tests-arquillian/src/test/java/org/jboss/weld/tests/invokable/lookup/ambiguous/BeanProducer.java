package org.jboss.weld.tests.invokable.lookup.ambiguous;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class BeanProducer {

    @Produces
    @MyQualifier5
    public String produceAmbig1() {
        throw new IllegalStateException("Ambiguous producer should never be invoked");
    }

    @Produces
    @MyQualifier5
    public String produceAmbig2() {
        throw new IllegalStateException("Ambiguous producer should never be invoked");
    }
}
