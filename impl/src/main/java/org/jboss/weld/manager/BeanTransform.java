/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.manager;

import java.util.function.Function;

import javax.enterprise.inject.spi.Bean;

final class BeanTransform implements Function<BeanManagerImpl, Iterable<Bean<?>>> {

    private final BeanManagerImpl declaringBeanManager;

    public BeanTransform(BeanManagerImpl declaringBeanManager) {
        this.declaringBeanManager = declaringBeanManager;
    }

    @Override
    public Iterable<Bean<?>> apply(BeanManagerImpl beanManager) {
        // New beans and built in beans aren't resolvable transitively
        if (beanManager.equals(declaringBeanManager)) {
            return beanManager.getBeans();
        } else {
            return beanManager.getSharedBeans();
        }
    }

}
