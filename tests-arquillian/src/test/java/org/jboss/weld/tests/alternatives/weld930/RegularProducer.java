package org.jboss.weld.tests.alternatives.weld930;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

/**
 * @author Marko Luksa
 */
@RequestScoped
public class RegularProducer {

    @Produces
    @Named("product")
    @RequestScoped
    public Product produce() throws Exception {
        return new Product("Regular");
    }
}
