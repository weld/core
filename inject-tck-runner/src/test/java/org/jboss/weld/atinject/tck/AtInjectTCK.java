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

package org.jboss.weld.atinject.tck;

import junit.framework.Test;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;
import org.jboss.arquillian.container.weld.embedded.mock.BeanDeploymentArchiveImpl;
import org.jboss.arquillian.container.weld.embedded.mock.FlatDeployment;
import org.jboss.arquillian.container.weld.embedded.mock.TestContainer;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import java.util.Arrays;

/**
 * Configure the AtInject TCK for use with the 299 RI
 *
 * @author pmuir
 */
public class AtInjectTCK {

    /**
     * Create JUnit TestSuite
     *
     * @return
     */
    public static Test suite() {
        // Create and start the TestContainer, which takes care of starting the container, deploying the
        // classes, starting the contexts etc.
        BeanDeploymentArchive archive = new BeanDeploymentArchiveImpl(AllDiscoveryBeansXml.INSTANCE, Arrays.asList(
                Convertible.class,
                Seat.class,
                DriversSeat.class,
                V8Engine.class,
                Cupholder.class,
                FuelTank.class,
                Tire.class,
                SpareTire.class
        ), Environments.SE);
        TestContainer container = new TestContainer(new FlatDeployment(archive, new AtInjectTCKExtension()));

        container.startContainer();

        // Our entry point is the single bean deployment archive
        BeanManager beanManager = container.getBeanManager(container.getDeployment().getBeanDeploymentArchives().iterator().next());

        // Obtain a reference to the Car and pass it to the TCK to generate the testsuite
        Bean<?> bean = beanManager.resolve(beanManager.getBeans(Car.class));
        Car instance = (Car) beanManager.getReference(bean, Car.class, beanManager.createCreationalContext(bean));

        return Tck.testsFor(instance, false /* supportsStatic */, true /* supportsPrivate */);
    }
}
