/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.extensions.staticObserver;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testing that static observers for non Lifecycle event in extension are called
 *
 * @author Antoine Sabot-Durand
 */
@RunWith(Arquillian.class)
public class StaticNonLifecycleEventObserverTest {

    private Set<String> stringSet = new HashSet<>();

    @Inject
    private Event<Set<String>> evt;

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(StaticNonLifecycleEventObserverTest.class))
                .addClass(ExtensionWithStaticNonLifecycleEventObserver.class)
                .addAsServiceProvider(Extension.class, ExtensionWithStaticNonLifecycleEventObserver.class);
    }

    @Test
    public void testSync() {
        evt.fire(stringSet);
        Assert.assertTrue(stringSet.contains(ExtensionWithStaticNonLifecycleEventObserver.SYNC));
    }

}
