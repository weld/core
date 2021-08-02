package org.jboss.weld.tests.alternatives.weld930;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

/**
 * @author Marko Luksa
 */
@Dependent
public class RegularProducer {

    @Produces
    @Named("product")
    public Product produce() throws Exception {
        return new Product("Regular");
    }
}
