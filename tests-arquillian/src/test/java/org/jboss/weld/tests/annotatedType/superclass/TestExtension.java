package org.jboss.weld.tests.annotatedType.superclass;

import org.jboss.weld.util.annotated.ForwardingAnnotatedType;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

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
