/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.event;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.experimental.Prioritized;
import org.jboss.weld.manager.BeanManagerImpl;

public class PrioritizedObserverMethodImpl<T, X> extends ObserverMethodImpl<T, X> implements Prioritized {

    private final int priority;

    protected PrioritizedObserverMethodImpl(final EnhancedAnnotatedMethod<T, ? super X> observer, final RIBean<X> declaringBean, final BeanManagerImpl manager, int priority) {
        super(observer, declaringBean, manager);
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PrioritizedObserverMethodImpl<?, ?> that = (PrioritizedObserverMethodImpl<?, ?>) obj;
        return super.equals(that);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
