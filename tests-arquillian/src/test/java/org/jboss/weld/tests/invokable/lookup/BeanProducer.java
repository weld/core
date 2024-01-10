package org.jboss.weld.tests.invokable.lookup;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class BeanProducer {

    @Produces
    @MyQualifier1("foo")
    @MyQualifier4("binding")
    public String produce1() {
        return MyQualifier1.class.getSimpleName() + MyQualifier4.class.getSimpleName();
    }

    @Produces
    @MyQualifier2
    public String produce2() {
        return MyQualifier2.class.getSimpleName();
    }

    @Produces
    public String producePlain() {
        throw new IllegalStateException("No qualifier producer should never be invoked");
    }

    @Produces
    @ToBeQualifier
    public String produceQualified() {
        return ToBeQualifier.class.getSimpleName();
    }
}
