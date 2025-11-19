package org.jboss.weld.tests.producer.alternative.priority.broken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;

@Dependent
public class BrokenBeanProducer {

    @Produces
    @Alternative
    @MyStereotype
    @MyOtherStereotype
    @ApplicationScoped
    public String produce() {
        return "broken";
    }

}
