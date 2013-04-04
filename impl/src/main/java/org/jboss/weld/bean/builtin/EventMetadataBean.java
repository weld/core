/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.builtin;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.event.CurrentEventMetadata;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Built-in bean for event metadata.
 *
 * @author Jozef Hartinger
 *
 */
public class EventMetadataBean extends AbstractStaticallyDecorableBuiltInBean<EventMetadata> {

    private final CurrentEventMetadata currentEventMetadata;

    public EventMetadataBean(BeanManagerImpl beanManager) {
        super(EventMetadata.class.getSimpleName(), beanManager, EventMetadata.class);
        this.currentEventMetadata = beanManager.getServices().get(CurrentEventMetadata.class);
    }

    @Override
    public void destroy(EventMetadata instance, CreationalContext<EventMetadata> creationalContext) {
    }

    @Override
    protected EventMetadata newInstance(InjectionPoint ip, CreationalContext<EventMetadata> creationalContext) {
        return currentEventMetadata.peek();
    }

    @Override
    public String toString() {
        return "Implicit Bean [" + EventMetadata.class.getName() + "] with qualifiers [@Default]";
    }
}
