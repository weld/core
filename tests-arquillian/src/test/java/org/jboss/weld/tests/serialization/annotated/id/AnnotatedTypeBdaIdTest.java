/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.weld.tests.serialization.annotated.id;

import java.io.IOException;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.annotated.slim.SlimAnnotatedType.SerializationProxy;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Verifies, that when {@link BeanManager#createAnnotatedType(Class)} is called on two different bean managers for the same
 * Class, the resulting
 * {@link AnnotatedType}s are equal.
 *
 * @see WELD-1600
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class AnnotatedTypeBdaIdTest {

    @Inject
    private CarFactory1 factory1;

    @Inject
    private CarFactory2 factory2;

    @Inject
    private BeanManagerImpl manager;

    @Deployment
    public static Archive<?> getDeployment() {
        JavaArchive jar1 = ShrinkWrap.create(BeanArchive.class).addClass(CarFactory1.class);
        JavaArchive jar2 = ShrinkWrap.create(BeanArchive.class).addClass(CarFactory2.class);
        JavaArchive nonBda = ShrinkWrap.create(JavaArchive.class).addClass(UnknownClass.class);
        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(AnnotatedTypeBdaIdTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addClasses(Car.class, Utils.class)
                .addAsLibraries(jar1, jar2, nonBda);
    }

    @Test
    public void testAnnotatedTypeIdsEqual() {
        Assert.assertEquals(factory1.produce(), factory2.produce());
    }

    @Test
    public void testLoadingNotKnownAnnotatedType() throws ClassNotFoundException, IOException {
        AnnotatedTypeIdentifier identifier = AnnotatedTypeIdentifier.forBackedAnnotatedType(manager.getContextId(),
                UnknownClass.class, UnknownClass.class, manager.getId());
        SerializationProxy<?> proxy = new SerializationProxy<Object>(identifier);
        Object result = Utils.deserialize(Utils.serialize(proxy));
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof AnnotatedType<?>);
    }
}
