package org.jboss.weld.tests.producer.method.broken.invalidBeanType;

import javax.enterprise.inject.Produces;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class MultiDimensionalTypeVariableArrayProducer {
    @Produces
    public static <T> T[][][] produceTypeVariableArray() {
        return null;
    }
}
