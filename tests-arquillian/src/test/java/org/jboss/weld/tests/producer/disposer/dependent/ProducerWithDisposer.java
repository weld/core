package org.jboss.weld.tests.producer.disposer.dependent;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

@Dependent
public class ProducerWithDisposer {

    @Produces
    public Product produce(DependentBean bean) {
        return new Product();
    }

    public void dispose(@Disposes Product product, DependentBean bean) {
    }
}
