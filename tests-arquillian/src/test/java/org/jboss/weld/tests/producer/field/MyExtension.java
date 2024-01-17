package org.jboss.weld.tests.producer.field;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

public class MyExtension implements Extension {

    public static int extensionTriggered = 0;

    public void observe(@Observes ProcessAnnotatedType<? extends FieldProducerBean> pat) {
        extensionTriggered++;
        pat.configureAnnotatedType();
    }
}
