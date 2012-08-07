package org.jboss.weld.tests.alternatives.weld930;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

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
