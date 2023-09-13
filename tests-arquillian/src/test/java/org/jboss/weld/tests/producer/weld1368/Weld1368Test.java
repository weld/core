package org.jboss.weld.tests.producer.weld1368;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.tests.producer.weld1368.SpecializedProducer.TestBean1;
import org.jboss.weld.tests.producer.weld1368.SpecializedProducer.TestBean3;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class Weld1368Test {

    @Inject
    private TestBean1 testBean1;

    @Deployment
    public static JavaArchive createDeployment() {

        return ShrinkWrap.create(JavaArchive.class)
                .addPackage(Weld1368Test.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

    }

    @Test
    public void testSpecializes() {
        String name1 = TestBean3.class.getName();
        String name2 = this.testBean1.getClass().getName();
        Assert.assertEquals(name1, name2);
    }

}
