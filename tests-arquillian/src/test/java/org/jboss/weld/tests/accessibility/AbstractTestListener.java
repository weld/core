/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.accessibility;

import javax.enterprise.inject.spi.Bean;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public abstract class AbstractTestListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // noop
    }

    protected Bean<?> getUniqueBean(Iterable<Bean<?>> beans, Class<?> beanClass) {
        Bean<?> result = null;
        for (Bean<?> bean : beans) {
            if (bean.getBeanClass().equals(beanClass)) {
                if (result != null) {
                    throw new IllegalStateException("Duplicate bean " + bean + ", " + result);
                }
                result = bean;
            }
        }
        return result;
    }

    protected boolean containsBean(Iterable<Bean<?>> beans, Class<?> beanClass) {
        return getUniqueBean(beans, beanClass) != null;
    }

    protected void assertTrue(boolean condition) {
        if (!condition) {
            throw new IllegalStateException("assertion failure");
        }
    }

    protected void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new IllegalStateException("Expected " + expected + " but was " + actual);
        }
    }
}
