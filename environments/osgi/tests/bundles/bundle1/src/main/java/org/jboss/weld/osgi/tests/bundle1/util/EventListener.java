/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.weld.osgi.tests.bundle1.util;

import org.jboss.weld.osgi.tests.bundle1.api.MovingService;
import org.jboss.weld.osgi.tests.bundle1.impl.MovingServiceImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import org.jboss.weld.environment.osgi.api.annotation.Publish;
import org.jboss.weld.environment.osgi.api.events.BundleContainerEvents;
import org.jboss.weld.environment.osgi.api.events.BundleEvents;
import org.jboss.weld.environment.osgi.api.events.Invalid;
import org.jboss.weld.environment.osgi.api.events.ServiceEvents;
import org.jboss.weld.environment.osgi.api.events.Valid;

@Publish
@ApplicationScoped
public class EventListener {

    public MovingService getMovingServiceInstance() {
        return new MovingServiceImpl();
    }

    private int start = 0;
    private int stop = 0;

    private void start(@Observes BundleContainerEvents.BundleContainerInitialized event) {
        start++;
    }

    private void stop(@Observes BundleContainerEvents.BundleContainerShutdown event) {
        stop++;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }

    private int serviceArrival = 0;
    private int serviceChanged = 0;
    private int serviceDeparture = 0;

    private void serviceArrival(@Observes ServiceEvents.ServiceArrival event) {
        serviceArrival++;
    }

    private void serviceChanged(@Observes ServiceEvents.ServiceChanged event) {
        serviceChanged++;
    }

    private void serviceDeparture(@Observes ServiceEvents.ServiceDeparture event) {
        serviceDeparture++;
    }

    public int getServiceArrival() {
        return serviceArrival;
    }

    public int getServiceChanged() {
        return serviceChanged;
    }

    public int getServiceDeparture() {
        return serviceDeparture;
    }

    private int bundleInstalled = 0;
    private int bundleUninstalled = 0;
    private int bundleResolved = 0;
    private int bundleUnresolved = 0;
    private int bundleStarting = 0;
    private int bundleStarted = 0;
    private int bundleStopping = 0;
    private int bundleStopped = 0;
    private int bundleUpdated = 0;
    private int bundleLazyActivation = 0;

    private void bundleInstalled (@Observes BundleEvents.BundleInstalled event) {
        bundleInstalled++;
    }

    private void bundleUninstalled (@Observes BundleEvents.BundleUninstalled event) {
        bundleUninstalled++;
    }

    private void bundleResolved (@Observes BundleEvents.BundleResolved event) {
        bundleResolved++;
    }

    private void bundleUnresolved (@Observes BundleEvents.BundleUnresolved event) {
        bundleUnresolved++;
    }

    private void bundleStarting (@Observes BundleEvents.BundleStarting event) {
        bundleStarting++;
    }

    private void bundleStarted (@Observes BundleEvents.BundleStarted event) {
        bundleStarted++;
    }

    private void bundleStopping (@Observes BundleEvents.BundleStopping event) {
        bundleStopping++;
    }

    private void bundleStopped (@Observes BundleEvents.BundleStopped event) {
        bundleStopped++;
    }

    private void bundleUpdated (@Observes BundleEvents.BundleUpdated event) {
        bundleUpdated++;
    }

    private void bundleLazyActivation (@Observes BundleEvents.BundleLazyActivation event) {
        bundleLazyActivation++;
    }

    public int getBundleInstalled() {
        return bundleInstalled;
    }

    public int getBundleUninstalled() {
        return bundleUninstalled;
    }

    public int getBundleResolved() {
        return bundleResolved;
    }

    public int getBundleUnresolved() {
        return bundleUnresolved;
    }

    public int getBundleStarting() {
        return bundleStarting;
    }

    public int getBundleStarted() {
        return bundleStarted;
    }

    public int getBundleStopping() {
        return bundleStopping;
    }

    public int getBundleStopped() {
        return bundleStopped;
    }

    public int getBundleUpdated() {
        return bundleUpdated;
    }

    public int getBundleLazyActivation() {
        return bundleLazyActivation;
    }

    private int bundleValid = 0;
    private int bundleInvalid = 0;

    private void bundleValid(@Observes Valid event) {
        bundleValid++;
    }

    private void bundleInvalid(@Observes Invalid event) {
        bundleInvalid++;
    }

    public int getBundleValid() {
        return bundleValid;
    }

    public int getBundleInvalid() {
        return bundleInvalid;
    }
}
