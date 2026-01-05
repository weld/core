package org.jboss.weld.environment.se.test.discovery.inheritedScope;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InheritedScopeReflectionDiscoveryStrategyTest {

    @Deployment
    public static Archive<?> createTestArchive() {

        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class)
                        .addClasses(InheritedScopeReflectionDiscoveryStrategyTest.class, ProperBean.class,
                                OnlyInheritedScopeBean.class))
                .build();
    }

    @Test
    public void testDiscovery() {
        Weld weld = new Weld()
                // must use annotated discovery!
                .setBeanDiscoveryMode(BeanDiscoveryMode.ANNOTATED);

        try (WeldContainer container = weld.initialize()) {
            Assert.assertTrue(container.isRunning());
            Assert.assertTrue(container.select(ProperBean.class).isResolvable());
            Assert.assertFalse(container.select(OnlyInheritedScopeBean.class).isResolvable());
        }
    }
}
