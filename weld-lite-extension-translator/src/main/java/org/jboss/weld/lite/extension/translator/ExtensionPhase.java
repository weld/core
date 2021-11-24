package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.Registration;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.Validation;

import java.lang.annotation.Annotation;

enum ExtensionPhase {
    DISCOVERY(Discovery.class),
    ENHANCEMENT(Enhancement.class),
    REGISTRATION(Registration.class),
    SYNTHESIS(Synthesis.class),
    VALIDATION(Validation.class),
    ;

    final Class<? extends Annotation> annotation;

    ExtensionPhase(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    @Override
    public String toString() {
        return "@" + annotation.getSimpleName();
    }
}
