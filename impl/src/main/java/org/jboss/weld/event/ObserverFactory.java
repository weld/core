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
package org.jboss.weld.event;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Bound factory class that produces implicit observers for observer methods.
 *
 * @author David Allen
 */
public class ObserverFactory {

    private ObserverFactory() {
    }

    /**
     * Creates an observer
     *
     * @param method The observer method abstraction
     * @param declaringBean The declaring bean
     * @param manager The Bean manager
     * @return An observer implementation built from the method abstraction
     */
    public static <T, X> ObserverMethodImpl<T, X> create(EnhancedAnnotatedMethod<T, ? super X> method, RIBean<X> declaringBean,
            BeanManagerImpl manager, boolean isAsync) {
        if (declaringBean instanceof ExtensionBean) {
            return new ExtensionObserverMethodImpl<T, X>(method, declaringBean, manager, isAsync);
        }
        return new ObserverMethodImpl<T, X>(method, declaringBean, manager, isAsync);
    }

    /**
     * Tests an observer method to see if it is transactional.
     *
     * @param observer The observer method
     * @return true if the observer method is annotated as transactional
     */
    public static TransactionPhase getTransactionalPhase(EnhancedAnnotatedMethod<?, ?> observer) {
        EnhancedAnnotatedParameter<?, ?> parameter = observer.getEnhancedParameters(Observes.class).iterator().next();
        return parameter.getAnnotation(Observes.class).during();
    }

}