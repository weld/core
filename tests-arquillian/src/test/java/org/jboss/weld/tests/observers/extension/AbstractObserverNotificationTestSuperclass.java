/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.observers.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractObserverNotificationTestSuperclass {

    @Inject
    private ObserverExtension extension;

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(AbstractObserverNotificationTestSuperclass.class))
                .addPackage(Giraffe.class.getPackage())
                .addAsServiceProvider(Extension.class, ObserverExtension.class);
    }

    @Before
    public void before() {
        reset();
    }

    /**
     * Template method so that we can test both events fired using BeanManager as well as the Event bean.
     */
    protected abstract void fireEvent(Giraffe payload, Annotation... qualifiers);

    @Test
    public void testNoQualifier() {
        Giraffe payload = new Giraffe();
        fireEvent(payload);
        verifyObserversNotNotified(extension.getFiveMeterTallGiraffeObserver(),
                extension.getSixMeterTallAngryGiraffeObserver(), extension.getAngryNubianGiraffeObserver());
        verifyObserversNotified(payload, extension.getAnyGiraffeObserver());
    }

    @Test
    public void testSingleQualifier() {
        Giraffe payload = new Giraffe();
        Tall qualifier = Tall.Literal.FIVE_METERS;

        fireEvent(payload, qualifier);
        verifyObserversNotNotified(extension.getSixMeterTallAngryGiraffeObserver(), extension.getAngryNubianGiraffeObserver());
        verifyObserversNotified(payload, extension.getAnyGiraffeObserver(),
                extension.getFiveMeterTallGiraffeObserver());
    }

    @Test
    public void testMultipleQualifiers() {
        Giraffe payload = new Giraffe();
        Set<Annotation> qualifiers = toSet(Tall.Literal.FIVE_METERS, new Angry.Literal(), new Nubian.Literal());

        fireEvent(payload, qualifiers.toArray(new Annotation[0]));
        verifyObserversNotNotified(extension.getSixMeterTallAngryGiraffeObserver());
        verifyObserversNotified(payload, extension.getAnyGiraffeObserver(),
                extension.getFiveMeterTallGiraffeObserver(), extension.getAngryNubianGiraffeObserver());
    }

    private void reset() {
        extension.getAnyGiraffeObserver().reset();
        extension.getFiveMeterTallGiraffeObserver().reset();
        extension.getSixMeterTallAngryGiraffeObserver().reset();
        extension.getAngryNubianGiraffeObserver().reset();
    }

    private void verifyObserversNotified(Giraffe payload, GiraffeObserver... observers) {
        for (GiraffeObserver observer : observers) {
            assertEquals(payload, observer.getReceivedPayload());
        }
    }

    private void verifyObserversNotNotified(GiraffeObserver... observers) {
        for (GiraffeObserver observer : observers) {
            assertNull(observer.getReceivedPayload());
        }
    }

    private Set<Annotation> toSet(Annotation... annotations) {
        return new HashSet<Annotation>(Arrays.asList(annotations));
    }
}
