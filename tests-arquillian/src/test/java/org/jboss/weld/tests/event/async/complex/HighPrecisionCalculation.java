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
package org.jboss.weld.tests.event.async.complex;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Vetoed;

@Vetoed // remove this to run a more precise calculation and see the result
public class HighPrecisionCalculation {

    public void configure(@Observes CalculationConfiguration configuration) {
        configuration.setNumberOfMessages(10000);
        configuration.setNumberOfElements(210000);
    }

    public void logResult(@Observes PiApproximation result) {
        System.out.println("Pi approximation: " + result.getPi());
        System.out.println("Calculation time: " + result.getDuration() + "ms");
    }
}
