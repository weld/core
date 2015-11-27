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

import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;

import org.jboss.weld.probe.tests.integration.deployment.annotations.Collector;

@ApplicationScoped
public class ApplicationScopedObserver {

    public void listen(@Observes @Collector("A") String action) {
    }

    public void listen1(@Observes @Collector("B") String action) {
    }

    public void listen2(@Observes @Collector("A") @Collector("B") String action) {
    }

    public void listen3(@Observes(notifyObserver = Reception.IF_EXISTS, during = TransactionPhase.BEFORE_COMPLETION) Properties properties) {
    }

    public void dummyObserver(@Observes DummyBean dummyBean){

    }

}
