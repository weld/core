/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.interceptors.producer;

import jakarta.enterprise.inject.Vetoed;

@Vetoed
public class InterfaceWithAnnotationImpl implements InterfaceWithAnnotation {

    // this method makes the class unproxyable because it is final
    private final String gandalfMethod() {
        return "You shall not proxy me!";
    }

    @Override
    public String ping() {
        return InterfaceWithAnnotationImpl.class.getSimpleName();
    }

    @Override
    public void pong() {
        //no-op
    }
}
