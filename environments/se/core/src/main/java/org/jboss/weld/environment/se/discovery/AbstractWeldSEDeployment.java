package org.jboss.weld.environment.se.discovery;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.environment.se.discovery.url.WeldSEResourceLoader;

import javax.enterprise.inject.spi.Extension;

/**
 * Implements the basic requirements of a {@link Deployment}. Provides a service
 * registry.
 * <p/>
 * Suitable for extension by those who need to build custom {@link Deployment}
 * implementations.
 *
 * @author Pete Muir
 * @author Ales Justin
 */
public abstract class AbstractWeldSEDeployment implements Deployment {

    public static final String BEANS_XML = "META-INF/beans.xml";
    public static final String[] RESOURCES = {BEANS_XML};

    private final ServiceRegistry serviceRegistry;
    private final Iterable<Metadata<Extension>> extensions;

    public AbstractWeldSEDeployment(Bootstrap bootstrap) {
        this.serviceRegistry = new SimpleServiceRegistry();
        this.extensions = bootstrap.loadExtensions(WeldSEResourceLoader.getClassLoader());
    }

    public ServiceRegistry getServices() {
        return serviceRegistry;
    }

    public Iterable<Metadata<Extension>> getExtensions() {
        return extensions;
    }


}