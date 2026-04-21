package org.jboss.weld.tests.eager.broken.stereotype;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@EagerDependentStereotype
public class DependentEagerBean {
}
