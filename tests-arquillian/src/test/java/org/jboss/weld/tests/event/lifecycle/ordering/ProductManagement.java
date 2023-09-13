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
package org.jboss.weld.tests.event.lifecycle.ordering;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.inject.spi.ProcessProducer;
import jakarta.enterprise.inject.spi.ProcessProducerMethod;

/**
 * CDI defines this order (see Bean Discovery chapter) for producers: PIP -> PP -> PBA -> PPM (e.g. ProcessBean for producer
 * methods)
 * And following order for ordinary beans: PIP -> PIT -> PBA -> PB
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class ProductManagement implements Extension {

    private List<Object> listOfProducerEvents = new ArrayList<>();
    private List<Object> listOfBeanEvents = new ArrayList<>();

    // producer method observers

    public void observePIP(@Observes ProcessInjectionPoint<PoorWorker, String> pip) {
        listOfProducerEvents.add(pip);
    }

    public void observePP(@Observes ProcessProducer<PoorWorker, HighQualityAndLowCostProduct> pp) {
        listOfProducerEvents.add(pp);
    }

    public void observePBA(@Observes ProcessBeanAttributes<HighQualityAndLowCostProduct> pba) {
        listOfProducerEvents.add(pba);
    }

    public void observerPPM(@Observes ProcessProducerMethod<HighQualityAndLowCostProduct, PoorWorker> ppm) {
        listOfProducerEvents.add(ppm);
    }

    // ordinary bean observers - PIP - PIT - PBA - PB

    public void observePIPBean(@Observes ProcessInjectionPoint<PoorWorker, MassiveJugCoffee> pip) {
        listOfBeanEvents.add(pip);
    }

    public void observePIT(@Observes ProcessInjectionTarget<PoorWorker> pit) {
        listOfBeanEvents.add(pit);
    }

    public void observePBABean(@Observes ProcessBeanAttributes<PoorWorker> pba) {
        listOfBeanEvents.add(pba);
    }

    public void observerPB(@Observes ProcessManagedBean<PoorWorker> pb) {
        listOfBeanEvents.add(pb);
    }

    public List<Object> getListOfProducerEvents() {
        return listOfProducerEvents;
    }

    public List<Object> getListOfBeanEvents() {
        return listOfBeanEvents;
    }
}
