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

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.jboss.weld.environment.osgi.api.events.BundleContainerEvents;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class CDIActivator {
    @Inject
    Timer timer;

    @Inject
    BundleContext context;

//   @Inject
//   @OSGiService
//   AutoPublishedService autoPublishedServiceInjected;
//
//   @Inject
//   @OSGiService
//   NotAutoPublishedService notAutoPublishedServiceInjected;
    public void start(@Observes BundleContainerEvents.BundleContainerInitialized evt) throws Exception {
        FlagFarm.cdiStartEntrance = FlagFarm.currentRank++;
        timer.process(500);
        FlagFarm.cdiStartExit = FlagFarm.currentRank++;

        if (timer != null) {
            FlagFarm.isCDIUnableInCDIStart = 1;
        }
        else {
            FlagFarm.isCDIUnableInCDIStart = 0;
        }

        if (context != null) {
            ServiceReference NotAutoPublishedServiceReference = context.getServiceReference(NotAutoPublishedService.class.getName());
            if (NotAutoPublishedServiceReference != null) {
                NotAutoPublishedService notAutoPublishedService = (NotAutoPublishedService) context.getService(NotAutoPublishedServiceReference);
                if (notAutoPublishedService != null && notAutoPublishedService.process()) {
                    FlagFarm.isOSGiUnableInCDIStart = 2;
                }
                else {
                    FlagFarm.isOSGiUnableInCDIStart = 1;
                }

            }
            else {
                FlagFarm.isOSGiUnableInCDIStart = 0;
            }

            ServiceReference AutoPublishedServiceReference = context.getServiceReference(AutoPublishedService.class.getName());
            if (AutoPublishedServiceReference != null) {
                AutoPublishedService autoPublishedService = (AutoPublishedService) context.getService(AutoPublishedServiceReference);
                if (autoPublishedService != null && autoPublishedService.process()) {
                    FlagFarm.isOSGiForAutoPublishedServiceUnableInCDIStart = 2;
                }
                else {
                    FlagFarm.isOSGiForAutoPublishedServiceUnableInCDIStart = 1;
                }

            }
            else {
                FlagFarm.isOSGiForAutoPublishedServiceUnableInCDIStart = 0;
            }
        }
        else {
            FlagFarm.isOSGiUnableInCDIStart = 0;
//         FlagFarm.isOSGiForAutoPublishedServiceUnableInCDIStart = 0;
        }
//
//      if (notAutoPublishedServiceInjected != null && notAutoPublishedServiceInjected.process())
//      {
//         FlagFarm.isWeldOSGiUnableInCDIStart = 1;
//      }
//      else
//      {
//         FlagFarm.isWeldOSGiUnableInCDIStart = 0;
//      }
//
//      if (autoPublishedServiceInjected != null && autoPublishedServiceInjected.process())
//      {
//         FlagFarm.isWeldOSGiForAutoPublishedServiceUnableInCDIStart = 1;
//      }
//      else
//      {
//         FlagFarm.isWeldOSGiForAutoPublishedServiceUnableInCDIStart = 0;
//      }
    }

    public void stop(@Observes BundleContainerEvents.BundleContainerShutdown evt) throws Exception {
        FlagFarm.cdiStopEntrance = FlagFarm.currentRank++;
        timer.process(500);
        FlagFarm.cdiStopExit = FlagFarm.currentRank++;

        if (timer != null) {
            FlagFarm.isCDIUnableInCDIStop = 1;
        }
        else {
            FlagFarm.isCDIUnableInCDIStop = 0;
        }

        if (context != null) {
            ServiceReference NotAutoPublishedServiceReference = context.getServiceReference(NotAutoPublishedService.class.getName());
            if (NotAutoPublishedServiceReference != null) {
                NotAutoPublishedService notAutoPublishedService = (NotAutoPublishedService) context.getService(NotAutoPublishedServiceReference);
                if (notAutoPublishedService != null && notAutoPublishedService.process()) {
                    FlagFarm.isOSGiUnableInCDIStop = 2;
                }
                else {
                    FlagFarm.isOSGiUnableInCDIStop = 1;
                }

            }
            else {
                FlagFarm.isOSGiUnableInCDIStop = 0;
            }

            ServiceReference AutoPublishedServiceReference = context.getServiceReference(AutoPublishedService.class.getName());
            if (AutoPublishedServiceReference != null) {
                AutoPublishedService autoPublishedService = (AutoPublishedService) context.getService(AutoPublishedServiceReference);
                if (autoPublishedService != null && autoPublishedService.process()) {
                    FlagFarm.isOSGiForAutoPublishedServiceUnableInCDIStop = 2;
                }
                else {
                    FlagFarm.isOSGiForAutoPublishedServiceUnableInCDIStop = 1;
                }

            }
            else {
                FlagFarm.isOSGiForAutoPublishedServiceUnableInCDIStop = 0;
            }
        }
        else {
            FlagFarm.isOSGiUnableInCDIStop = 0;
//         FlagFarm.isOSGiForAutoPublishedServiceUnableInCDIStop = 0;
        }

//      if (notAutoPublishedServiceInjected != null && notAutoPublishedServiceInjected.process())
//      {
//         FlagFarm.isWeldOSGiUnableInCDIStop = 1;
//      }
//      else
//      {
//         FlagFarm.isWeldOSGiUnableInCDIStop = 0;
//      }
//
//      if (autoPublishedServiceInjected != null && autoPublishedServiceInjected.process())
//      {
//         FlagFarm.isWeldOSGiForAutoPublishedServiceUnableInCDIStop = 1;
//      }
//      else
//      {
//         FlagFarm.isWeldOSGiForAutoPublishedServiceUnableInCDIStop = 0;
//      }
    }

}
