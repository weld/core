package org.jboss.weld.environment.se.test.beandiscovery;

import static org.junit.Assert.assertEquals;

import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.environment.se.test.arquillian.WeldSEClassPath;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeanDiscoveryEmtyBeansXmlTest {
    @Deployment
    public static Archive<?> getDeployment() {
        JavaArchive archive01 = ShrinkWrap.create(BeanArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(Dog.class, Cat.class, Cow.class);
        JavaArchive archive02 = ShrinkWrap.create(BeanArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(Plant.class, Tree.class, Stone.class);
        JavaArchive archive03 = ShrinkWrap.create(BeanArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(Flat.class, House.class);
        return ShrinkWrap.create(WeldSEClassPath.class).add(archive01, archive02, archive03);
    }

    @Test
    public void testArchive01(BeanManager manager) {
        assertEquals(1, manager.getBeans(Dog.class).size());
        assertEquals(1, manager.getBeans(Cat.class).size());
        assertEquals(1, manager.getBeans(Cow.class).size());
    }

    @Test
    public void testArchive02(BeanManager manager) {
        assertEquals(1, manager.getBeans(Tree.class).size());
        assertEquals(1, manager.getBeans(Plant.class).size());
        assertEquals(0, manager.getBeans(Stone.class).size());
    }

    @Test
    public void testArchive03(BeanManager manager) {
        assertEquals(1, manager.getBeans(Flat.class).size());
        assertEquals(1, manager.getBeans(House.class).size());
    }
}
