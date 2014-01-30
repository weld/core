package org.jboss.weld.environment.servlet.test.util;

import javax.servlet.ServletContainerInitializer;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.environment.servlet.EnhancedListener;

public class TomcatDeployments {

    public static final Asset CONTEXT_XML = new StringAsset("<Context crossContext=\"true\"> <Manager pathname=\"\" /> <Resource name=\"BeanManager\" auth=\"Container\" type=\"javax.inject.manager.BeanManager\" factory=\"org.jboss.weld.resources.ManagerObjectFactory\"/></Context>");

    /**
     *
     * @return an archive which helps to work around Tomcat embedded and Maven Surefire classloading issue
     */
    public static JavaArchive createWorkaroundEnhancedListenerArchive() {
        return ShrinkWrap.create(JavaArchive.class).addClass(EnhancedListener.class)
                .addAsManifestResource(new StringAsset(EnhancedListener.class.getName()), "services/" + ServletContainerInitializer.class.getName());
    }

}
