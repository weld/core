/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.weld.osgi.examples.calculator.log;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.jboss.weld.environment.osgi.api.annotation.Specification;
import org.jboss.weld.environment.osgi.api.events.InterBundleEvent;
import org.jboss.weld.osgi.examples.calculator.api.CleanEvent;
import org.jboss.weld.osgi.examples.calculator.api.NotificationEvent;
import org.jboss.weld.osgi.examples.calculator.api.Operation;

@ApplicationScoped
public class ActionsLogger {

   @Inject
   private Event<InterBundleEvent> ibEvent;

    public void listenToEquals(@Observes @Specification(Operation.class) InterBundleEvent event) {
        Operation op = event.typed(Operation.class).get();

        ibEvent.fire(new InterBundleEvent(new NotificationEvent(op.getValue1()
                + " " + op.getOperator().label()
                + " " + op.getValue2()
                + " = " + op.value())));
    }

    public void listenToClean(@Observes @Specification(CleanEvent.class) InterBundleEvent event) {
        ibEvent.fire(new InterBundleEvent(new NotificationEvent("Clean")));
    }
}
