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
package org.jboss.weld.tests.resolution.circular.self;

import jakarta.ejb.Singleton;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * @author Kirill Gaevskii
 *
 */
@Dependent
@Singleton
public class SingletonSelfInjectionBean {
    @Inject
    private SingletonSelfInjectionBean self;

    public Integer methodA(Integer value) {
        return self.methodC(value);
    }

    public Integer methodB(Integer value) {
        return this.methodC(value);
    }

    @SelfIntercept
    public Integer methodC(Integer value) {
        return value;
    }
}
