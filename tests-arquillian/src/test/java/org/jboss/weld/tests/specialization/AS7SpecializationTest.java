package org.jboss.weld.tests.specialization;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * @author mmazi
 * @author alesj
 */
@RunWith(Arquillian.class)
public class AS7SpecializationTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsLibrary(ShrinkWrap.create(BeanArchive.class, "test.jar").addClass(User.class))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClass(User2.class);
    }

    @Inject
    private BeanManager beanManager;

    /**
     * WELD-321, WELD-912
     */
    @Test
    public void testSpecialization() {
        Assert.assertEquals(User2.class, beanManager.resolve(beanManager.getBeans(User.class)).getBeanClass());
    }
}
