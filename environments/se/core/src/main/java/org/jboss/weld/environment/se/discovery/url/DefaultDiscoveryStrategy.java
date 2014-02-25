package org.jboss.weld.environment.se.discovery.url;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.resources.spi.ResourceLoader;

public class DefaultDiscoveryStrategy extends DiscoveryStrategy {

    public DefaultDiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap) {
        super(resourceLoader, bootstrap);
    }

    protected void manageAnnotatedDiscovery(BeanArchiveBuilder builder) {
        throw new IllegalArgumentException("Cannot handle Annotated bean discovery.");
    }

}
