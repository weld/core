package org.jboss.weld.tests.annotatedType.superclass;

import org.jboss.weld.util.annotated.ForwardingAnnotatedType;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.util.Collections;
import java.util.Set;

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
