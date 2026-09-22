package org.jboss.weld.tests.bootstrap.unusedbeans.rest;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.configuration.spi.ExternalConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JakartaRestUnusedBeanTest {
    @Deployment
    public static Archive<?> deploy() {
        // WildFly supplies its own ExternalConfiguration, so set excludeType through weld.properties.
        // CleanupConfiguration enables integrator-only optimized cleanup for the embedded runner;
        // WildFly already enables it in its own configuration.
        return ShrinkWrap.create(BeanArchive.class)
                .addClasses(JakartaRestUnusedBeanTest.class.getDeclaredClasses())
                .addAsServiceProvider(Service.class, CleanupConfiguration.class)
                .addAsResource(new StringAsset(ConfigurationKey.UNUSED_BEANS_EXCLUDE_TYPE.get() + "="
                        + ConfigurationKey.UnusedBeans.NONE), "weld.properties");
    }

    @Inject
    BeanManager manager;

    @Test
    public void defaultAnnotationPatternProtectsJakartaRestBeans() {
        assertEquals(1, manager.getBeans(RestResource.class).size());
        assertEquals(1, manager.getBeans(RestMethod.class).size());
        assertEquals(0, manager.getBeans(Unused.class).size());
    }

    @ApplicationScoped
    @Path("/resource")
    public static class RestResource {
    }

    @ApplicationScoped
    public static class RestMethod {
        @GET
        public String get() {
            return "value";
        }
    }

    @ApplicationScoped
    public static class Unused {
    }

    public static class CleanupConfiguration implements ExternalConfiguration {
        public Map<String, Object> getConfigurationProperties() {
            return Map.of(ConfigurationKey.ALLOW_OPTIMIZED_CLEANUP.get(), true);
        }

        public void cleanup() {
        }
    }
}
