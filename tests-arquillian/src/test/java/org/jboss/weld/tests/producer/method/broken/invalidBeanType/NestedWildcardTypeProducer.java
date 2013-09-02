package org.jboss.weld.tests.producer.method.broken.invalidBeanType;

import javax.enterprise.inject.Produces;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class NestedWildcardTypeProducer {
    @Produces
    public static Foo<Foo<?>> produceWildcardFooFoo() {
        return null;
    }
}
