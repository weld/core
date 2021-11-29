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

package org.jboss.weld.tests.extensions.custombeans;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;

/**
 * Serves as a counter of how many times a pre-destroy was called.
 * Initialized only from within extension's produceWith and disposeWith methods via Instance
 */
@Dependent
public class DependentBean {

    public static int TIMES_DESTROY_INVOKED = 0;

    public static void resetCounter() {
        TIMES_DESTROY_INVOKED = 0;
    }

    @PreDestroy
    public void destroy() {
        TIMES_DESTROY_INVOKED++;
    }
}
