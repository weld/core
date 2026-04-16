package org.jboss.weld.tests.bce.syntheticInjectionPoint.broken;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * A bean that exists with @Default qualifier but NOT with @MyQualifier.
 */
@ApplicationScoped
public class ExistingDefaultBean {
}
