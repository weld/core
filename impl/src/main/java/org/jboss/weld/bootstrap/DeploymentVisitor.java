package org.jboss.weld.bootstrap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.spi.Context;

import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.collections.WeldCollections;

/**
 * A Deployment visitor which can find the transitive closure of Bean Deployment Archives
 *
 * @author pmuir
 */
public class DeploymentVisitor {

    private final BeanManagerImpl deploymentManager;
    private final Environment environment;
    private final Deployment deployment;
    private final BeanDeploymentArchiveMapping bdaMapping;
    private final Collection<ContextHolder<? extends Context>> contexts;

    public DeploymentVisitor(BeanManagerImpl deploymentManager, Environment environment, final Deployment deployment,
            Collection<ContextHolder<? extends Context>> contexts,
            BeanDeploymentArchiveMapping bdaMapping) {
        this.deploymentManager = deploymentManager;
        this.environment = environment;
        this.deployment = deployment;
        this.contexts = contexts;
        this.bdaMapping = bdaMapping;
    }

    public void visit() {
        Set<BeanDeploymentArchive> seenBeanDeploymentArchives = new HashSet<BeanDeploymentArchive>();
        for (BeanDeploymentArchive archive : deployment.getBeanDeploymentArchives()) {
            if (!seenBeanDeploymentArchives.contains(archive)) {
                visit(archive, seenBeanDeploymentArchives);
            }
        }
        // Alhough it's the responsibility of an integrator, check the uniqueness to avoid weird bugs
        if (bdaMapping.isNonuniqueIdentifierDetected()) {
            throw BootstrapLogger.LOG
                    .nonuniqueBeanDeploymentIdentifier(WeldCollections.toMultiRowString(bdaMapping.getBeanDeployments()));
        }
    }

    private BeanDeployment visit(BeanDeploymentArchive bda, Set<BeanDeploymentArchive> seenBeanDeploymentArchives) {
        copyService(bda, ResourceLoader.class);
        // Check that the required services are specified
        WeldStartup.verifyServices(bda.getServices(), environment.getRequiredBeanDeploymentArchiveServices(), bda.getId());

        // Check the id is not null
        if (bda.getId() == null) {
            throw BootstrapLogger.LOG.deploymentArchiveNull(bda);
        }

        BeanDeployment parent = bdaMapping.getBeanDeployment(bda);
        if (parent == null) {
            // Create the BeanDeployment
            parent = new BeanDeployment(bda, deploymentManager, deployment.getServices(), contexts);

            // Attach it
            bdaMapping.put(bda, parent);
        }

        seenBeanDeploymentArchives.add(bda);

        for (BeanDeploymentArchive archive : bda.getBeanDeploymentArchives()) {
            BeanDeployment child;
            // Cut any circularities
            if (!seenBeanDeploymentArchives.contains(archive)) {
                child = visit(archive, seenBeanDeploymentArchives);
            } else {
                // already visited
                child = bdaMapping.getBeanDeployment(archive);
            }
            parent.getBeanManager().addAccessibleBeanManager(child.getBeanManager());
        }
        return parent;
    }

    private <T extends Service> void copyService(BeanDeploymentArchive archive, Class<T> serviceClass) {
        // for certain services we can fall back to deployment-level settings or defaults
        ServiceRegistry registry = archive.getServices();
        if (!registry.contains(serviceClass)) {
            T service = deployment.getServices().get(serviceClass);
            if (service != null) {
                registry.add(serviceClass, service);
            }
        }
    }

}
