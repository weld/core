package org.jboss.weld.tests.alternatives.weld930;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

/**
 * @author Marko Luksa
 */
public class RegularProducer {

    @Produces
    @Named("product")
    public Product produce() throws Exception {
        return new Product("Regular");
    }
}
