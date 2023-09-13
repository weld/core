package org.jboss.weld.tests.contexts.retrieval;

import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.spi.Context;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests retrieval of registered instances of contexts for given scope annotations.
 *
 * See also https://github.com/jakartaee/cdi/issues/628
 */
@RunWith(Arquillian.class)
public class WeldManagerGetContextsTest {

    @Deployment
    public static WebArchive getDeployment() {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(WeldManagerGetContextsTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(WeldManagerGetContextsTest.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    WeldManager weldManager;

    @Test
    public void testContextObjectRetrieval() {
        // there is only one app context in Weld and is always active
        Collection<Context> contexts = weldManager.getContexts(ApplicationScoped.class);
        Assert.assertEquals(1, contexts.size());
        Assert.assertTrue(contexts.iterator().next().isActive());

        // There are four different req. contexts in Weld, only one is active during the test though
        contexts = weldManager.getContexts(RequestScoped.class);
        Assert.assertEquals(4, contexts.size());
        Assert.assertEquals(1, contexts.stream().filter(c -> c.isActive()).count());
    }
}
