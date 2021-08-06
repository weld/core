/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.cleanup;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;

public class TestExtension implements Extension {

    static final AtomicBoolean PIT_OBSERVED = new AtomicBoolean(false);

   // This observer is always dropped after bootstrap
   void observeFooPat(@Observes ProcessAnnotatedType<Foo> event) {
       event.configureAnnotatedType().removeAll().add(ApplicationScoped.Literal.INSTANCE);
   }

   // This observer is only dropped if optimized cleanup is allowed
   void observeFooPit(@Observes ProcessInjectionTarget<Foo> event) {
       PIT_OBSERVED.set(true);
   }

}
