package org.jboss.weld.tests.producer.method.broken.invalidBeanType;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class NestedTypeVariableProducer {
    @Produces
    @RequestScoped
    public static <T> Foo<Foo<T>> produceTypeVariableFooFoo() {
        return null;
    }
}
