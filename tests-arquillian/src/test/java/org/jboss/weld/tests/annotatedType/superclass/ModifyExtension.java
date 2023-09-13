package org.jboss.weld.tests.annotatedType.superclass;

import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

import org.jboss.weld.util.annotated.ForwardingAnnotatedType;

/**
 * @author Ales Justin
 */
public class ModifyExtension implements Extension {
    public void processAnnotatedType(@Observes ProcessAnnotatedType<Child> event) {
        final AnnotatedType<Child> annotatedType = event.getAnnotatedType();
        event.setAnnotatedType(new ForwardingAnnotatedType<Child>() {
            @Override
            public AnnotatedType<Child> delegate() {
                return annotatedType;
            }

            @Override
            public Set<AnnotatedField<? super Child>> getFields() {
                return Collections.emptySet();
            }
        });
    }
}
