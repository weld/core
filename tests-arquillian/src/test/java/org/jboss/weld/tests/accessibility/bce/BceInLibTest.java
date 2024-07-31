package org.jboss.weld.tests.accessibility.bce;

import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.accessibility.bce.lib.MyBce;
import org.jboss.weld.tests.accessibility.bce.lib.MyBeanCreator;
import org.jboss.weld.tests.accessibility.bce.lib.SomeType;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Tests a scenario in which an application (WAR) has a library (/lib) which isn't a bean archive but has registered BCE.
 * While such BCE creates synth bean, its {@code Instance} should be able to access beans from the application (WAR)
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class BceInLibTest {

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(BceInLibTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(MyBean.class)
                .addAsWebInfResource(new BeansXml(BeanDiscoveryMode.ANNOTATED), "beans.xml");
        // archive with extension and no beans.xml == not a bean archive
        JavaArchive lib = ShrinkWrap.create(JavaArchive.class)
                .addClasses(MyBce.class, MyBeanCreator.class, SomeType.class)
                .addAsServiceProvider(BuildCompatibleExtension.class, MyBce.class);
        return war.addAsLibrary(lib);
    }

    @Inject
    Instance<Object> instance;

    @Test
    public void testVisibility() {
        // assert dummy bean is available
        assertTrue(instance.select(MyBean.class).isResolvable());
        // assert we can select type added through BCE from here
        assertTrue(instance.select(SomeType.class).isResolvable());
    }

}
