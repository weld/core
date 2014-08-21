package org.jboss.weld.environment.deployment.discovery;

import java.util.Set;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.resources.spi.ClassFileServices;

/**
 *
 * @author Matej Briškár
 * @author Martin Kouba
 */
public interface DiscoveryStrategy {

    /**
     * Optionally, a client may set a custom scanner implementation. If not set, the impl is allowed to use anything it considers appropriate.
     *
     * @param beanArchiveScanner
     */
    void setScanner(BeanArchiveScanner beanArchiveScanner);

    /**
     *
     * @return the set of discovered {@link BeanDeploymentArchive}s
     */
    Set<WeldBeanDeploymentArchive> performDiscovery();

    /**
     *
     * @return the associated {@link ClassFileServices} or <code>null</code>
     */
    ClassFileServices getClassFileServices();

}
