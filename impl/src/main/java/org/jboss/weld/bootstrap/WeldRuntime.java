package org.jboss.weld.bootstrap;

import java.util.Map;

import javax.enterprise.inject.spi.CDI;

import org.jboss.weld.Container;
import org.jboss.weld.ContainerState;
import org.jboss.weld.Weld;
import org.jboss.weld.bootstrap.events.BeforeShutdownImpl;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.context.ApplicationContext;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * @author Pete Muir
 * @author Ales Justin
 * @author Marko Luksa
 */
public class WeldRuntime {

    private BeanManagerImpl deploymentManager;
    private Map<BeanDeploymentArchive, BeanManagerImpl> bdaToBeanManagerMap;
    private String contextId;

    public WeldRuntime(String contextId, BeanManagerImpl deploymentManager, Map<BeanDeploymentArchive, BeanManagerImpl> bdaToBeanManagerMap) {
        this.contextId = contextId;
        this.deploymentManager = deploymentManager;
        this.bdaToBeanManagerMap = bdaToBeanManagerMap;
    }

    public BeanManagerImpl getManager(BeanDeploymentArchive beanDeploymentArchive) {
        BeanManagerImpl beanManager = bdaToBeanManagerMap.get(beanDeploymentArchive);
        return beanManager == null ? null : beanManager.getCurrent();
    }

    public void shutdown() {
        try {
            // First, the container must destroy all contexts.
            deploymentManager.instance().select(ApplicationContext.class).get().invalidate();
        } finally {
            try {
                // Finally, the container must fire an event of type BeforeShutdown.
                BeforeShutdownImpl.fire(deploymentManager);
            } finally {
                Container container = Container.instance(contextId);
                container.setState(ContainerState.SHUTDOWN);
                container.cleanup();
                // remove BeanManager references
                try {
                    CDI<?> cdi = CDI.current();
                    if (cdi instanceof Weld) {
                        ((Weld) cdi).cleanup();
                    }
                } catch (java.lang.IllegalStateException ignored) {
                }
            }
        }
    }
}
