/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.injectionPoint.resource;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.Dependent;

import org.jboss.weld.test.util.ActionSequence;

@Dependent
public class Charlie {

    private String anotherGreeting;

    protected String getAnotherGreeting() {
        return anotherGreeting;
    }

    @Resource(name = "org.jboss.weld.tests.injectionPoint.resource.Alpha/greeting")
    protected void setAnotherGreeting(String greeting) {
        this.anotherGreeting = greeting;
        ActionSequence.addAction(Charlie.class.getName() + String.class.getName());
    }

}
