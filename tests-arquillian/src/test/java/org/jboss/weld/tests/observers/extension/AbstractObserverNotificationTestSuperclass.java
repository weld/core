/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.observers.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.literal.AnyLiteral;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractObserverNotificationTestSuperclass {

    @Inject
    private ObserverExtension extension;

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(Giraffe.class.getPackage())
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
        verifyObserversNotified(payload, toSet(), extension.getAnyGiraffeObserver());
    }

    @Test
    public void testSingleQualifier() {
        Giraffe payload = new Giraffe();
        Tall qualifier = Tall.Literal.FIVE_METERS;

        fireEvent(payload, qualifier);
        verifyObserversNotNotified(extension.getSixMeterTallAngryGiraffeObserver(), extension.getAngryNubianGiraffeObserver());
        verifyObserversNotified(payload, toSet(qualifier), extension.getAnyGiraffeObserver(),
                extension.getFiveMeterTallGiraffeObserver());
    }

    @Test
    public void testMultipleQualifiers() {
        Giraffe payload = new Giraffe();
        Set<Annotation> qualifiers = toSet(Tall.Literal.FIVE_METERS, new Angry.Literal(), new Nubian.Literal());

        fireEvent(payload, qualifiers.toArray(new Annotation[0]));
        verifyObserversNotNotified(extension.getSixMeterTallAngryGiraffeObserver());
        verifyObserversNotified(payload, qualifiers, extension.getAnyGiraffeObserver(),
                extension.getFiveMeterTallGiraffeObserver(), extension.getAngryNubianGiraffeObserver());
    }

    private void reset() {
        extension.getAnyGiraffeObserver().reset();
        extension.getFiveMeterTallGiraffeObserver().reset();
        extension.getSixMeterTallAngryGiraffeObserver().reset();
        extension.getAngryNubianGiraffeObserver().reset();
    }

    private void verifyObserversNotified(Giraffe payload, Set<Annotation> qualifiers, GiraffeObserver... observers) {
        Set<Annotation> expectedQualifiers = processQualifiers(qualifiers);
        for (GiraffeObserver observer : observers) {
            assertFalse(observer.isLegacyNotifyCalled());
            assertEquals(payload, observer.getReceivedPayload());
            assertEquals(expectedQualifiers, observer.getReceivedQualifiers());
        }
    }

    private void verifyObserversNotNotified(GiraffeObserver... observers) {
        for (GiraffeObserver observer : observers) {
            assertFalse(observer.isLegacyNotifyCalled());
            assertNull(observer.getReceivedPayload());
            assertNull(observer.getReceivedQualifiers());
        }
    }

    private Set<Annotation> toSet(Annotation... annotations) {
        return new HashSet<Annotation>(Arrays.asList(annotations));
    }

    protected Set<Annotation> processQualifiers(Set<Annotation> qualifiers) {
        qualifiers.add(AnyLiteral.INSTANCE); // every event has this qualifier
        return qualifiers;
    }
}
