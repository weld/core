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
package org.jboss.weld.bootstrap;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.event.ObserverMethodImpl;

public class ObserverInitializationContext<T, X> {

    public static <T, X> ObserverInitializationContext<T, X> of(ObserverMethodImpl<T, X> observer,
            EnhancedAnnotatedMethod<T, ? super X> annotated) {
        return new ObserverInitializationContext<T, X>(observer, annotated);
    }

    private final ObserverMethodImpl<T, X> observer;
    private final EnhancedAnnotatedMethod<T, ? super X> annotated;

    public ObserverInitializationContext(ObserverMethodImpl<T, X> observer, EnhancedAnnotatedMethod<T, ? super X> annotated) {
        this.observer = observer;
        this.annotated = annotated;
    }

    public void initialize() {
        observer.initialize(annotated);
    }

    public ObserverMethodImpl<T, X> getObserver() {
        return observer;
    }
}
