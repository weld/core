package org.jboss.weld.tests.event;

import java.lang.annotation.Annotation;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.EventMetadata;

@ApplicationScoped
public class ObservingBean {

    private volatile int defaultObjectNotified = 0;
    private volatile int defaultObjectAsyncNotified = 0;
    private volatile int defaultPayloadNotified = 0;
    private volatile int defaultPayloadAsyncNotified = 0;
    private volatile Set<Annotation> defaultObjectQualifiers;
    private volatile Set<Annotation> defaultObjectAsyncQualifiers;
    private volatile Set<Annotation> defaultPayloadQualifiers;
    private volatile Set<Annotation> defaultPayloadAsyncQualifiers;

    public void observeDefaultObject(@Observes @Default Object payload, EventMetadata em) {
        // object type is very broad, only look for Payload runtime type
        if (em.getType().equals(Payload.class)) {
            this.defaultObjectNotified++;
            this.defaultObjectQualifiers = em.getQualifiers();
        }
    }

    public void observeDefaultPayload(@Observes @Default Payload payload, EventMetadata em) {
        this.defaultPayloadNotified++;
        this.defaultPayloadQualifiers = em.getQualifiers();
    }

    public void observeDefaultObjectAsync(@ObservesAsync @Default Object payload, EventMetadata em) {
        // object type is very broad, only look for Payload runtime type
        if (em.getType().equals(Payload.class)) {
            this.defaultObjectAsyncNotified++;
            this.defaultObjectAsyncQualifiers = em.getQualifiers();
        }
    }

    public void observeDefaultPayloadAsync(@ObservesAsync @Default Payload payload, EventMetadata em) {
        this.defaultPayloadAsyncNotified++;
        this.defaultPayloadAsyncQualifiers = em.getQualifiers();
    }

    public int getDefaultObjectNotified() {
        return defaultObjectNotified;
    }

    public int getDefaultPayloadNotified() {
        return defaultPayloadNotified;
    }

    public Set<Annotation> getDefaultObjectQualifiers() {
        return defaultObjectQualifiers;
    }

    public Set<Annotation> getDefaultPayloadQualifiers() {
        return defaultPayloadQualifiers;
    }

    public int getDefaultObjectAsyncNotified() {
        return defaultObjectAsyncNotified;
    }

    public int getDefaultPayloadAsyncNotified() {
        return defaultPayloadAsyncNotified;
    }

    public Set<Annotation> getDefaultObjectAsyncQualifiers() {
        return defaultObjectAsyncQualifiers;
    }

    public Set<Annotation> getDefaultPayloadAsyncQualifiers() {
        return defaultPayloadAsyncQualifiers;
    }

    public void reset() {
        this.defaultPayloadNotified = 0;
        this.defaultPayloadAsyncNotified = 0;
        this.defaultObjectNotified = 0;
        this.defaultObjectAsyncNotified = 0;
        this.defaultObjectQualifiers = null;
        this.defaultObjectAsyncQualifiers = null;
        this.defaultPayloadQualifiers = null;
        this.defaultPayloadAsyncQualifiers = null;
    }
}
