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
package org.jboss.weld.environment.osgi.impl.extension.beans;

import org.jboss.weld.environment.osgi.api.events.InterBundleEvent;
import org.jboss.weld.environment.osgi.spi.CDIContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.Collection;

/**
 * Store Weld containers of all bean bundle. Broadcast {@link InterBundleEvent}
 * from current bean bundle to others.
 * <p/>
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
@ApplicationScoped
public class ContainerObserver {

    private static Logger logger =
                          LoggerFactory.getLogger(ContainerObserver.class);

    private CDIContainer currentContainer;

    private Collection<CDIContainer> containers;

    public void setCurrentContainer(CDIContainer currentContainer) {
        this.currentContainer = currentContainer;
    }

    public void setContainers(Collection<CDIContainer> containers) {
        this.containers = containers;
    }

    public void listenInterBundleEvents(@Observes InterBundleEvent event) {
        logger.trace("Entering DynamicServiceHandler : "
                     + "DynamicServiceHandler() with parameter {}",
                     new Object[] {event});
        if (!event.isSent()) {
            logger.debug("Broadcasting the inter bundle event {} from bundle {}",
                         event,
                         currentContainer.getBundle());
            event.sent();
            for (CDIContainer container : containers) {
                if (!container.equals(currentContainer)) {
                    try {
                        logger.trace("Broadcasting the inter bundle event {} "
                                     + "to bundle {}",
                                     event,
                                     container.getBundle());
                        container.fire(event);
                    }
                    catch(Throwable t) {
                        logger.warn("InterBundle event broadcast failed", t);
                    }
                }
            }
        }
    }

}
