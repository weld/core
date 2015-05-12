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
package org.jboss.weld.injection.producer;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.reflection.Formats;

public abstract class AbstractProducer<T> implements Producer<T> {

    protected void checkDelegateInjectionPoints() {
        for (InjectionPoint injectionPoint : getInjectionPoints()) {
            if (injectionPoint.isDelegate()) {
                throw BeanLogger.LOG.delegateNotOnDecorator(injectionPoint, Formats.formatAsStackTraceElement(injectionPoint));
            }
        }
    }

    public abstract Annotated getAnnotated();

    /**
     * Returns a {@link Bean} this producer is associated with or null if no such bean exists.
     */
    public abstract Bean<T> getBean();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getAnnotated() == null) ? 0 : getAnnotated().hashCode());
        result = prime * result + ((getBean() == null) ? 0 : getBean().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractProducer<?> other = (AbstractProducer<?>) obj;
        if (getAnnotated() == null) {
            if (other.getAnnotated() != null) {
                return false;
            }
        } else {
            if (!getAnnotated().equals(other.getAnnotated())) {
                return false;
            }
        }
        if (getBean() == null) {
            if (other.getBean() != null) {
                return false;
            }
        } else if (!getBean().equals(other.getBean())) {
            return false;
        }
        return true;
    }
}
