package org.jboss.weld.tests.producer.method.broken.invalidBeanType;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
@Dependent
public class NestedTypeVariableProducer {
    @Produces
    @RequestScoped
    public static <T> Foo<Foo<T>> produceTypeVariableFooFoo() {
        return null;
    }
}
