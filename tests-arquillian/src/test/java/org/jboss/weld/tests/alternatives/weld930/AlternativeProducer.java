package org.jboss.weld.tests.alternatives.weld930;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

/**
 * @author Marko Luksa
 */
@Alternative
public class AlternativeProducer {

    @Produces
    @Named("product")
    public Product produce() throws Exception {
        return new Product("Alternative");
    }
}
