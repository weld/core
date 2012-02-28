package org.jboss.weld.tests.alternatives.weld930;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

/**
 * @author Marko Luksa
 */
@RequestScoped
@Alternative
public class AlternativeProducer {

    @Produces
    @Named("product")
    @RequestScoped
    public Product produce() throws Exception {
        return new Product("Alternative");
    }
}
