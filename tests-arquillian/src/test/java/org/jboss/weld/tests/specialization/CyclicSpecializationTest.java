package org.jboss.weld.tests.specialization;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author alesj
 */
@RunWith(Arquillian.class)
public class CyclicSpecializationTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(CyclicSpecializationTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addAsLibrary(ShrinkWrap.create(BeanArchive.class, "test.jar").addClasses(User2.class, Account.class))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(User.class, Account2.class);
    }

    @Inject
    private BeanManager beanManager;

    /**
     * WELD-912
     */
    @Test
    public void testSpecialization() {
        Assert.assertEquals(User2.class, beanManager.resolve(beanManager.getBeans(User.class)).getBeanClass());
        Assert.assertEquals(Account2.class, beanManager.resolve(beanManager.getBeans(Account.class)).getBeanClass());
    }
}
