package org.jboss.weld.environment.se.test.isolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.shrinkwrap.impl.BeansXml.Exclude;
import org.jboss.weld.environment.se.test.arquillian.WeldSEClassPath;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

/*
 * Verifies that the archives are merged in disabled archive isolation mode ({@link Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY} set to false).
 */
@RunWith(Arquillian.class)
public class IsolationDisabledTest extends ArchiveIsolationOverrideTestBase {

    @Override
    public boolean isArchiveIsolationEnabled() {
        return false;
    }

    @Deployment(managed = false)
    public static Archive<?> getDeployment() {
        WeldSEClassPath archives = ShrinkWrap.create(WeldSEClassPath.class);

        JavaArchive archive01 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ALL)
                        .interceptors(ZoomInterceptor.class)
                        .decorators(CameraDecorator.class)
                        .alternatives(RangefinderCamera.class)
                        .excludeFilters(
                                Exclude.exact(PinholeCamera.class).ifClassAvailable(DSLR.class)),
                        "beans.xml")
                .addClasses(ZoomInterceptor.class, CameraDecorator.class, RangefinderCamera.class);

        JavaArchive archive02 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ALL), "beans.xml")
                .addClasses(Zoom.class, Camera.class, DSLR.class, PinholeCamera.class);

        archives.add(archive01);
        archives.add(archive02);
        return archives;
    }

    @After
    public void resetInvocations() {
        CameraDecorator.invocations = 0;
        ZoomInterceptor.invocations = 0;
    }

    @Test
    public void testInterceptorAndDecorator01(DSLR camera) {
        camera.capture();
        assertEquals(1, CameraDecorator.invocations);
        assertEquals(1, ZoomInterceptor.invocations);
    }

    @Test
    public void testInterceptorAndDecorator02(RangefinderCamera camera) {
        camera.capture();
        assertEquals(1, CameraDecorator.invocations);
        assertEquals(0, ZoomInterceptor.invocations);
    }

    @Test
    public void testAlternative(BeanManager bm) {
        Set<Class<?>> cameras = getBeanClasses(bm, Camera.class);
        assertTrue(cameras.contains(DSLR.class));
        assertTrue(cameras.contains(RangefinderCamera.class));
        assertEquals(2, cameras.size());
    }

    @Test
    public void testExcludeFilters(BeanManager bm) {
        assertFalse(getBeanClasses(bm, Camera.class).contains(PinholeCamera.class));
    }

    private Set<Class<?>> getBeanClasses(BeanManager bm, Type beanType, Annotation... annotations) {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for (Bean<?> bean : bm.getBeans(beanType, annotations)) {
            classes.add(bean.getBeanClass());
        }
        return classes;
    }

}
