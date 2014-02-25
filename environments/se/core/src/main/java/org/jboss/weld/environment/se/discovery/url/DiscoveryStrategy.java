package org.jboss.weld.environment.se.discovery.url;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.se.discovery.AbstractWeldSEDeployment;
import org.jboss.weld.environment.se.discovery.WeldSEBeanDeploymentArchive;
import org.jboss.weld.resources.spi.ResourceLoader;

public abstract class DiscoveryStrategy {

    private ResourceLoader resourceLoader;
    private Bootstrap bootstrap;
    private Collection<BeanArchiveBuilder> builders;
    public static final String[] RESOURCES = { AbstractWeldSEDeployment.BEANS_XML };
    private List<BeanDeploymentArchive> deploymentArchives = new ArrayList<BeanDeploymentArchive>();

    public DiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap) {
        this.resourceLoader = resourceLoader;
        this.bootstrap = bootstrap;
    }

    public Collection<BeanDeploymentArchive> discoverArchive() throws UnrecognizedBeansXmlDiscoveryModeException {
        builders = new URLScanner(resourceLoader, bootstrap, AbstractWeldSEDeployment.RESOURCES).scan();
        initialize();
        for (BeanArchiveBuilder builder : builders) {
            BeansXml beansXml = builder.parseBeansXml();
            switch (beansXml.getBeanDiscoveryMode()) {
                case ALL:
                    manageAllDiscovery(builder);
                    break;
                case ANNOTATED:
                    manageAnnotatedDiscovery(builder);
                    break;
                case NONE:
                    manageNoneDiscovery(builder);
                    break;
                default:
                    throw new UnrecognizedBeansXmlDiscoveryModeException("beans.xml has undefined bean discovery value");
            }
        }
        assignVisibility(deploymentArchives);
        return deploymentArchives;
    }

    private void assignVisibility(List<BeanDeploymentArchive> deploymentArchives) {
        for (BeanDeploymentArchive archive : deploymentArchives) {
            ((WeldSEBeanDeploymentArchive) archive).setBeanDeploymentArchives(deploymentArchives);
        }

    }

    public Collection<BeanArchiveBuilder> getBuilders() {
        return builders;
    }

    protected void addToArchives(WeldSEBeanDeploymentArchive bda) {
        deploymentArchives.add(bda);
    }

    /*
     * Methods to be overriden by the subclasses
     */

    protected void initialize() {
        // method for overriding
    }

    protected void manageNoneDiscovery(BeanArchiveBuilder builder) {
    }

    protected void manageAnnotatedDiscovery(BeanArchiveBuilder builder) {
        // method for overriding
    }

    protected void manageAllDiscovery(BeanArchiveBuilder builder) {
        WeldSEBeanDeploymentArchive bda = builder.build();
        addToArchives(bda);
    }


}
