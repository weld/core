/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.probe.tests.integration.deployment.beans;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.weld.probe.tests.integration.deployment.annotations.Collector;
import org.jboss.weld.probe.tests.integration.deployment.interceptors.TestInterceptorBinding;

@SessionScoped
public class SessionScopedBean implements Serializable {

    public static final String MESSAGE_A = "A happened.";
    public static final String MESSAGE_B = "B happened.";
    public static final String MESSAGE_AB = "AB happened.";

    public static final String SOME_METHOD_NAME = "doSomething";
    public static final String GETTER_METHOD_NAME = "getTestJavaAccesorMethods";

    public String getTestJavaAccesorMethods() {
        return testJavaAccesorMethods;
    }

    private String testJavaAccesorMethods = "message";

    @Inject
    Event<String> event;

    @Inject
    Event<DummyBean> dummyBeanEvent;

    @TestInterceptorBinding
    public void doSomething() {

        Collector.CollectorLiteral collectorLiteralA = new Collector.CollectorLiteral("A");
        event.select(collectorLiteralA).fire(MESSAGE_A);

        Collector.CollectorLiteral collectorLiteralB = new Collector.CollectorLiteral("B");
        event.select(collectorLiteralB).fire(MESSAGE_B);

        event.select(collectorLiteralA).select(collectorLiteralB).fire(MESSAGE_AB);
        dummyBeanEvent.fire(new DummyBean());

    }
}
