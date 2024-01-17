package org.jboss.weld.tests.producer.field;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

@Dependent
public class FieldProducerBean {

    @Produces
    @ApplicationScoped
    FieldProducerExtensionTest.Foo p = new FieldProducerExtensionTest.Foo();
}
