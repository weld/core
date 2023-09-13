package org.jboss.weld.tests.annotatedType.superclass;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

import org.jboss.weld.util.annotated.ForwardingAnnotatedType;

/**
 * @author Gert Palok
 */
public class TestExtension implements Extension {
    public void processAnnotatedType(@Observes ProcessAnnotatedType<Child> event) {
        final AnnotatedType<Child> annotatedType = event.getAnnotatedType();
        event.setAnnotatedType(new ForwardingAnnotatedType<Child>() {
            @Override
            public AnnotatedType<Child> delegate() {
                return annotatedType;
            }
        });
    }
}
