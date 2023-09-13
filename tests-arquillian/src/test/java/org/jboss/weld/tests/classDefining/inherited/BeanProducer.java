/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.classDefining.inherited;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.jboss.weld.tests.classDefining.inherited.base.AncestorInterface;
import org.jboss.weld.tests.classDefining.inherited.extending.MyInterface;

@ApplicationScoped
public class BeanProducer {

    // produce a proxied bean for a type with interface hierarchy
    @Produces
    @ApplicationScoped
    public MyInterface produceBean() {
        return new MyInterface() {
            @Override
            public String anotherPing() {
                return MyInterface.class.getSimpleName();
            }

            @Override
            public String ping() {
                return AncestorInterface.class.getSimpleName();
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AMuchBetterPrincipal producePrincipal() {
        return new AMuchBetterPrincipal() {
            @Override
            public String getName() {
                return AMuchBetterPrincipal.class.getSimpleName();
            }
        };
    }
}
