package org.jboss.weld.tests.producer.method.weld994;

import javax.enterprise.inject.Produces;

/**
 * @author Marko Luksa
 */
public class UnserializableObjectProducer {

    @Produces
    public UnserializableObject produce() {
        return new UnserializableObject("foo");
    }
}
