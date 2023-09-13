/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.serialization.spi.BeanIdentifier;

/**
 * A {@link Bean} implementation provided by Weld.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the type of the bean instance
 */
public interface WeldBean<T> extends Bean<T> {

    /**
     * @return the {@link BeanIdentifier} for this bean
     */
    BeanIdentifier getIdentifier();

    /**
     * Used for custom beans registered via WeldBeanConfigurator.
     *
     * @return bean priority or null if not set or overriden
     */
    default Integer getPriority() {
        return null;
    }
}
