/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.tests.jpms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.enterprise.inject.se.SeContainer;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.tests.jpms.beans.ApplicationScopedBean;
import org.jboss.weld.tests.jpms.beans.ConstructorInjectedBean;
import org.jboss.weld.tests.jpms.beans.DependentBean;
import org.jboss.weld.tests.jpms.beans.FieldInjectedBean;
import org.jboss.weld.tests.jpms.beans.InterceptedBean;
import org.jboss.weld.tests.jpms.beans.LoggingInterceptor;
import org.jboss.weld.tests.jpms.closed.ClosedPackageBean;
import org.junit.jupiter.api.Test;

public class JpmsIntegrationIT {

    @Test
    public void testBeansOnModulePath() {
        Weld weld = new Weld().disableDiscovery()
                .packages(ApplicationScopedBean.class)
                .interceptors(LoggingInterceptor.class);
        try (SeContainer container = weld.initialize()) {
            ApplicationScopedBean appScoped = container.select(ApplicationScopedBean.class).get();
            assertEquals("hello", appScoped.hello());
            assertNotEquals(ApplicationScopedBean.class, appScoped.getClass(),
                    "ApplicationScoped bean should be a proxy");

            DependentBean dependent = container.select(DependentBean.class).get();
            assertEquals("dependent", dependent.value());

            FieldInjectedBean fieldInjected = container.select(FieldInjectedBean.class).get();
            assertEquals("dependent", fieldInjected.delegated());

            ConstructorInjectedBean ctorInjected = container.select(ConstructorInjectedBean.class).get();
            assertEquals("dependent", ctorInjected.delegated());

            LoggingInterceptor.invoked = false;
            InterceptedBean intercepted = container.select(InterceptedBean.class).get();
            assertEquals("intercepted", intercepted.work());
            assertTrue(LoggingInterceptor.invoked, "Interceptor should have been invoked");
        }
    }

    @Test
    public void testMissingOpensGivesClearErrorMessage() {
        try {
            Weld weld = new Weld().disableDiscovery()
                    .packages(ClosedPackageBean.class);
            try (SeContainer container = weld.initialize()) {
                container.select(ClosedPackageBean.class).get();
                fail("Should have thrown — package is not opened");
            }
        } catch (Exception e) {
            String msgs = collectMessages(e);
            assertTrue(msgs.contains("opens"),
                    "Error chain should mention 'opens' directive, got: " + msgs);
            assertTrue(msgs.contains("org.jboss.weld.tests.jpms.closed"),
                    "Error chain should mention the closed package, got: " + msgs);
        }
    }

    private String collectMessages(Throwable t) {
        StringBuilder sb = new StringBuilder();
        while (t != null) {
            if (t.getMessage() != null) {
                sb.append(t.getMessage()).append(" ");
            }
            t = t.getCause();
        }
        return sb.toString();
    }
}
