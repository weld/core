package org.jboss.weld.tests.specialization;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Matus Abaffy
 */
@RunWith(Arquillian.class)
public class IndirectSpecializationNameInheritedTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(IndirectSpecializationNameInheritedTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(BeanA.class, BeanB.class, BeanC.class, IndirectSpecializationNameInheritedTest.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private BeanManager beanManager;

    @Inject
    BeanA beanA;

    /**
     * WELD-1562
     */
    @Test
    public void testSpecialization() {
        Assert.assertEquals(BeanC.class, beanManager.resolve(beanManager.getBeans("beanA")).getBeanClass());

        BeanC beanC = (BeanC) beanA;
        Assert.assertEquals("PropA from BeanC here!", beanC.getPropA());
        Assert.assertEquals("PropB from BeanC here!", beanC.getPropB());
        Assert.assertEquals("PropC from BeanC here!", beanC.getPropC());
    }
}
