/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.environment.servlet.test.isolation;

import static org.jboss.weld.environment.servlet.test.util.Deployments.extendDefaultWebXml;
import static org.jboss.weld.environment.servlet.test.util.Deployments.toContextParam;
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
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.shrinkwrap.impl.BeansXml.Exclude;
import org.jboss.weld.environment.servlet.test.util.Deployments;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that the archives are merged in disabled archive isolation mode.
 */
@RunWith(Arquillian.class)
public class IsolationDisabledTest {

    @Deployment
    public static WebArchive createTestArchive() {

        WebArchive testArchive = Deployments.baseDeployment(
                new StringAsset(
                        extendDefaultWebXml(toContextParam("org.jboss.weld.environment.servlet.archive.isolation", "false"))))
                .addClass(
                        IsolationDisabledTest.class);

        JavaArchive archive01 = ShrinkWrap
                .create(BeanArchive.class)
                .addAsManifestResource(
                        new BeansXml(BeanDiscoveryMode.ALL).interceptors(ZoomInterceptor.class)
                                .decorators(CameraDecorator.class)
                                .alternatives(RangefinderCamera.class)
                                .excludeFilters(Exclude.exact(PinholeCamera.class).ifClassAvailable(DSLR.class)),
                        "beans.xml")
                .addClasses(ZoomInterceptor.class, CameraDecorator.class, RangefinderCamera.class);

        JavaArchive archive02 = ShrinkWrap.create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ALL), "beans.xml")
                .addClasses(Zoom.class, Camera.class, DSLR.class, PinholeCamera.class);

        testArchive.addAsLibraries(archive01, archive02);
        return testArchive;
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
