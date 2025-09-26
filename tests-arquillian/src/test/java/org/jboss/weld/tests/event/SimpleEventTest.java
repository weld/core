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
package org.jboss.weld.tests.event;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SimpleEventTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SimpleEventTest.class))
                .addPackage(SimpleEventTest.class.getPackage());
    }

    private static boolean RECEIVE_1_OBSERVED;
    private static boolean RECEIVE_2_OBSERVED;
    private static boolean RECEIVE_3_OBSERVED;

    private static void initFlags() {
        RECEIVE_1_OBSERVED = false;
        RECEIVE_2_OBSERVED = false;
        RECEIVE_3_OBSERVED = false;
    }

    @Inject
    private BeanManagerImpl beanManager;

    @Test
    public void testFireEventOnManager() {
        initFlags();

        beanManager.getEvent().select(String.class, new AnnotationLiteral<Updated>() {
        }).fire("Fired using Manager Interface with AnnotationLiteral.");

        assert RECEIVE_1_OBSERVED == true;
        assert RECEIVE_2_OBSERVED == true;
        assert RECEIVE_3_OBSERVED == true;

        initFlags();

        beanManager.getEvent().select(String.class).fire("Fired using Manager Interface.");

        assert RECEIVE_1_OBSERVED == false; // not called
        assert RECEIVE_2_OBSERVED == true;
        assert RECEIVE_3_OBSERVED == true;
    }

    @Test
    public void testFireEventOnEvent(App app) {
        initFlags();

        app.fireEventByBindingDeclaredAtInjectionPoint();

        assert RECEIVE_1_OBSERVED == true;
        assert RECEIVE_2_OBSERVED == true;
        assert RECEIVE_3_OBSERVED == true;

        initFlags();

        app.fireEventByAnnotationLiteral();

        assert RECEIVE_1_OBSERVED == true;
        assert RECEIVE_2_OBSERVED == true;
        assert RECEIVE_3_OBSERVED == true;

        initFlags();

        app.fireEventViaAny();

        assert RECEIVE_2_OBSERVED == true;
        assert RECEIVE_1_OBSERVED == false; // not called
        assert RECEIVE_3_OBSERVED == true;

        initFlags();

        app.fireEventViaWithNoQualifier();

        assert RECEIVE_1_OBSERVED == false; // not called
        assert RECEIVE_2_OBSERVED == true;
        assert RECEIVE_3_OBSERVED == true;
    }

    @Dependent
    public static class App {
        @Inject
        @Any
        Event<String> event1;

        @Inject
        @Updated
        Event<String> event2;

        @Inject
        Event<String> event4;

        public void fireEventByAnnotationLiteral() {
            event1.select(new AnnotationLiteral<Updated>() {
            }).fire("Fired using Event Interface with AnnotationLiteral.");
        }

        public void fireEventByBindingDeclaredAtInjectionPoint() {
            event2.fire("Fired using Event Interface with Binding Declared.");
        }

        public void fireEventViaAny() {
            event1.fire("Fired using Event Interface");
        }

        public void fireEventViaWithNoQualifier() {
            event4.fire("Fired using Event Interface with no qualifier");
        }
    }

    @Dependent
    public static class Receiver {

        public void receive1(@Observes @Updated String s) {
            RECEIVE_1_OBSERVED = true;
        }

        public void receive2(@Any @Observes String s) {
            RECEIVE_2_OBSERVED = true;
        }

        public void receive3(@Observes String s) {
            RECEIVE_3_OBSERVED = true;
        }
    }
}
