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
package org.jboss.weld.tests.event.observer.weld2338;

import jakarta.enterprise.inject.spi.BeanManager;
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
 * Before WELD-2338, this use case resulted in DefinitionException as the OM was recognized as container lifecycle observer
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class ObserverInExtensionTest {

    @Inject
    BeanManager bm;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ObserverInExtensionTest.class))
                .addPackage(ObserverInExtensionTest.class.getPackage())
                .addAsServiceProvider(Extension.class, Observer.class);
    }

    @Test
    public void testObserversAreRecognizedCorrectly() {
        // firstly, @Observes Object o  should have notifications from container event already
        int cleNotified = Observer.timesCleNotified.get();
        Assert.assertTrue(cleNotified > 1);
        Assert.assertEquals(0, Observer.timesNonCleNotified.get());

        // then we fire additional event and see if @Observers @Experimental Object   was notified
        bm.getEvent().select(Payload.class, Experimental.Literal.INSTANCE).fire(new Payload());
        Assert.assertTrue(Observer.nonCleFooInjected.get());
        Assert.assertEquals(1, Observer.timesNonCleNotified.get());
        // @Observes Object  should get notification as well
        Assert.assertTrue(cleNotified < Observer.timesCleNotified.get());
    }
}
