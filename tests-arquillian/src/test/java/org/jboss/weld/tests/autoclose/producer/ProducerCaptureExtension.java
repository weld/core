package org.jboss.weld.tests.autoclose.producer;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessProducer;
import jakarta.enterprise.inject.spi.Producer;

public class ProducerCaptureExtension implements Extension {

    private static volatile Producer<?> capturedProducer;

    public void observeProducer(@Observes ProcessProducer<ResourceProducer, CloseableResource> event) {
        if (event.getAnnotatedMember().isAnnotationPresent(AutoCloseProducerQualifier.class)) {
            capturedProducer = event.getProducer();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Producer<T> getCapturedProducer() {
        return (Producer<T>) capturedProducer;
    }

    public static void reset() {
        capturedProducer = null;
    }
}
