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
package org.jboss.weld.osgi.tests.lifecycle;

import javax.inject.Inject;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class OSGiActivator implements BundleActivator {
    Timer timer = new Timer();

    @Inject
    Timer injected;

//   @Inject
//   @OSGiService
//   AutoPublishedService autoPublishedServiceInjected;
//
//   @Inject
//   @OSGiService
//   NotAutoPublishedService notAutoPublishedServiceInjected;
    AsynchronousListener asynchronousListener = new AsynchronousListener();

    SynchronousListener synchronousListener = new SynchronousListener();

    @Override
    public void start(BundleContext context) throws Exception {
        context.addBundleListener(asynchronousListener);
        context.addBundleListener(synchronousListener);

        context.registerService(NotAutoPublishedService.class.getName(), new NotAutoPublishedService(), null);

        FlagFarm.osgiStartEntrance = FlagFarm.currentRank++;
        timer.process(500);
        FlagFarm.osgiStartExit = FlagFarm.currentRank++;

        if (injected != null) {
            FlagFarm.isCDIUnableInOSGiStart = 1;
        }
        else {
            FlagFarm.isCDIUnableInOSGiStart = 0;
        }

        ServiceReference NotAutoPublishedServiceReference = context.getServiceReference(NotAutoPublishedService.class.getName());
        if (NotAutoPublishedServiceReference != null) {
            NotAutoPublishedService notAutoPublishedService = (NotAutoPublishedService) context.getService(NotAutoPublishedServiceReference);
            if (notAutoPublishedService != null && notAutoPublishedService.process()) {
                FlagFarm.isOSGiUnableInOSGiStart = 2;
            }
            else {
                FlagFarm.isOSGiUnableInOSGiStart = 1;
            }

        }
        else {
            FlagFarm.isOSGiUnableInOSGiStart = 0;
        }

        ServiceReference AutoPublishedServiceReference = context.getServiceReference(AutoPublishedService.class.getName());
        if (AutoPublishedServiceReference != null) {
            AutoPublishedService autoPublishedService = (AutoPublishedService) context.getService(AutoPublishedServiceReference);
            if (autoPublishedService != null && autoPublishedService.process()) {
                FlagFarm.isOSGiForAutoPublishedServiceUnableInOSGiStart = 2;
            }
            else {
                FlagFarm.isOSGiForAutoPublishedServiceUnableInOSGiStart = 1;
            }

        }
        else {
            FlagFarm.isOSGiForAutoPublishedServiceUnableInOSGiStart = 0;
        }

//      if(notAutoPublishedServiceInjected != null && notAutoPublishedServiceInjected.process()) {
//         FlagFarm.isWeldOSGiUnableInOSGiStart = 1;
//      } else {
//         FlagFarm.isWeldOSGiUnableInOSGiStart = 0;
//      }
//
//      if(autoPublishedServiceInjected != null && autoPublishedServiceInjected.process()) {
//         FlagFarm.isWeldOSGiForAutoPublishedServiceUnableInOSGiStart = 1;
//      } else {
//         FlagFarm.isWeldOSGiForAutoPublishedServiceUnableInOSGiStart = 0;
//      }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        FlagFarm.osgiStopEntrance = FlagFarm.currentRank++;
        timer.process(1000);
        FlagFarm.osgiStopExit = FlagFarm.currentRank++;

        if (injected != null) {
            FlagFarm.isCDIUnableInOSGiStop = 1;
        }
        else {
            FlagFarm.isCDIUnableInOSGiStop = 0;
        }

        ServiceReference NotAutoPublishedServiceReference = context.getServiceReference(NotAutoPublishedService.class.getName());
        if (NotAutoPublishedServiceReference != null) {
            NotAutoPublishedService notAutoPublishedService = (NotAutoPublishedService) context.getService(NotAutoPublishedServiceReference);
            if (notAutoPublishedService != null && notAutoPublishedService.process()) {
                FlagFarm.isOSGiUnableInOSGiStop = 2;
            }
            else {
                FlagFarm.isOSGiUnableInOSGiStop = 1;
            }

        }
        else {
            FlagFarm.isOSGiUnableInOSGiStop = 0;
        }

        ServiceReference AutoPublishedServiceReference = context.getServiceReference(AutoPublishedService.class.getName());
        if (AutoPublishedServiceReference != null) {
            AutoPublishedService autoPublishedService = (AutoPublishedService) context.getService(AutoPublishedServiceReference);
            if (autoPublishedService != null && autoPublishedService.process()) {
                FlagFarm.isOSGiForAutoPublishedServiceUnableInOSGiStop = 2;
            }
            else {
                FlagFarm.isOSGiForAutoPublishedServiceUnableInOSGiStop = 1;
            }

        }
        else {
            FlagFarm.isOSGiForAutoPublishedServiceUnableInOSGiStop = 0;
        }

//      if(notAutoPublishedServiceInjected != null && notAutoPublishedServiceInjected.process()) {
//         FlagFarm.isWeldOSGiUnableInOSGiStop = 1;
//      } else {
//         FlagFarm.isWeldOSGiUnableInOSGiStop = 0;
//      }
//
//      if(autoPublishedServiceInjected != null && autoPublishedServiceInjected.process()) {
//         FlagFarm.isWeldOSGiForAutoPublishedServiceUnableInOSGiStop = 1;
//      } else {
//         FlagFarm.isWeldOSGiForAutoPublishedServiceUnableInOSGiStop = 0;
//      }
    }

}
