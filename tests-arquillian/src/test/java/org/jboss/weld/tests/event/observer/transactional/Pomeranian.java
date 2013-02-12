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
package org.jboss.weld.tests.event.observer.transactional;

import java.io.Serializable;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import static javax.ejb.TransactionManagementType.BEAN;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import static javax.enterprise.event.TransactionPhase.*;

@Stateful
@TransactionManagement(BEAN)
@Tame
@SessionScoped
@SuppressWarnings("serial")
public class Pomeranian implements PomeranianInterface, Serializable {

    private static final Logger log = Logger.getLogger(Pomeranian.class.getName());

    @Override
    public void observeInProgress(@Observes(during = IN_PROGRESS) Bark event) {
        Actions.add(IN_PROGRESS);
    }

    /**
     * Observes a String event only after the transaction is completed.
     *
     * @param someEvent
     */
    @Override
    public void observeAfterCompletion(@Observes(during = AFTER_COMPLETION) Bark someEvent) {
        Actions.add(AFTER_COMPLETION);
    }

    /**
     * Observes an Integer event if the transaction is successfully completed.
     *
     * @param event
     */
    @Override
    public void observeAfterSuccess(@Observes(during = AFTER_SUCCESS) Bark event) {
        Actions.add(AFTER_SUCCESS);
    }

    /**
     * Observes a Float event only if the transaction failed.
     *
     * @param event
     */
    @Override
    public void observeAfterFailure(@Observes(during = AFTER_FAILURE) Bark event) {
        Actions.add(AFTER_FAILURE);
    }

    @Override
    public void observeBeforeCompletion(@Observes(during = BEFORE_COMPLETION) Bark event) {
        Actions.add(BEFORE_COMPLETION);
    }
    
    @Override
    public void observeAndFail(@Observes(during=BEFORE_COMPLETION) @Gnarly Bark event) throws FooException {
        Actions.add(BEFORE_COMPLETION);
        throw new FooException();
    }
}
