package org.jboss.weld.tests.producer.method.broken.invalidBeanType;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Dependent
public class WildcardTypeArrayProducer {
    @Produces
    public Foo<?>[] produceWildcardFooArray() {
        return null;
    }
}
