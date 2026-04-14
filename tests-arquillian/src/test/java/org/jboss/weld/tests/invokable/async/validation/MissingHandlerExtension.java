package org.jboss.weld.tests.invokable.async.validation;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Extension;

public class MissingHandlerExtension implements Extension {

    public void validate(@Observes AfterDeploymentValidation adv) {
        // This type has no registered async handler — should cause a deployment problem
        adv.ensureAsyncHandlerExists(NoSuchAsyncType.class);
    }
}
