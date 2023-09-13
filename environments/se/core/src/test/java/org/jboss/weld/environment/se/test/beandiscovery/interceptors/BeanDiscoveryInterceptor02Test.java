package org.jboss.weld.environment.se.test.beandiscovery.interceptors;

import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.environment.se.test.arquillian.WeldSEClassPath;
import org.jboss.weld.environment.se.test.beandiscovery.Cat;
import org.jboss.weld.environment.se.test.beandiscovery.Dog;
import org.jboss.weld.environment.se.test.beandiscovery.Flat;
import org.jboss.weld.environment.se.test.beandiscovery.House;
import org.jboss.weld.environment.se.test.beandiscovery.Plant;
import org.jboss.weld.environment.se.test.beandiscovery.Stone;
import org.jboss.weld.environment.se.test.beandiscovery.Tree;
import org.jboss.weld.environment.se.test.isolation.ArchiveIsolationOverrideTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeanDiscoveryInterceptor02Test extends ArchiveIsolationOverrideTestBase {

    @Override
    public boolean isArchiveIsolationEnabled() {
        return true;
    }

    @Deployment(managed = false)
    public static Archive<?> getDeployment() {
        WeldSEClassPath archives = ShrinkWrap.create(WeldSEClassPath.class);
        JavaArchive archive01 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(
                        new BeansXml(BeanDiscoveryMode.ALL).interceptors(ScopedInterceptor.class, ClassicInterceptor.class),
                        "beans.xml")
                .addClasses(Dog.class, Cat.class, ClassicInterceptor.class, ScopedInterceptor.class,
                        InterceptorBindingAnnotation.class);
        JavaArchive archive02 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ANNOTATED).interceptors(ScopedInterceptor.class,
                        ClassicInterceptor.class), "beans.xml")
                .addClasses(Plant.class, Tree.class, Stone.class);
        JavaArchive archive03 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(
                        new BeansXml(BeanDiscoveryMode.NONE).interceptors(ScopedInterceptor.class, ClassicInterceptor.class),
                        "beans.xml")
                .addClasses(Flat.class, House.class);
        archives.add(archive01);
        archives.add(archive02);
        archives.add(archive03);
        return archives;
    }

    /**
     * Test bean discovery in SE.
     */
    @Test
    public void testAllBeanDiscovery(Dog dog) {
        int classicInterceptorCalls = ClassicInterceptor.called;
        int scopedInterceptorCalls = ScopedInterceptor.called;
        dog.bark();
        assertEquals(classicInterceptorCalls + 1, ClassicInterceptor.called);
        assertEquals(scopedInterceptorCalls + 1, ScopedInterceptor.called);
    }

    @Test
    public void testAnnotatedBeanDiscovery(Plant plant, Tree tree) {
        int classicInterceptorCalls = ClassicInterceptor.called;
        int scopedInterceptorCalls = ScopedInterceptor.called;
        plant.getHeigh();
        assertEquals(classicInterceptorCalls + 1, ClassicInterceptor.called);
        assertEquals(scopedInterceptorCalls + 1, ScopedInterceptor.called);
        tree.grow();
        assertEquals(classicInterceptorCalls + 1, ClassicInterceptor.called);
        assertEquals(scopedInterceptorCalls + 1, ScopedInterceptor.called);

    }

}