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

import static jakarta.ejb.TransactionManagementType.BEAN;
import static jakarta.enterprise.event.TransactionPhase.AFTER_COMPLETION;
import static jakarta.enterprise.event.TransactionPhase.AFTER_FAILURE;
import static jakarta.enterprise.event.TransactionPhase.AFTER_SUCCESS;
import static jakarta.enterprise.event.TransactionPhase.BEFORE_COMPLETION;
import static jakarta.enterprise.event.TransactionPhase.IN_PROGRESS;

import java.io.Serializable;

import jakarta.annotation.Priority;
import jakarta.ejb.Stateful;
import jakarta.ejb.TransactionManagement;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;

@Stateful
@TransactionManagement(BEAN)
@Tame
@SessionScoped
@SuppressWarnings("serial")
public class Pomeranian implements PomeranianInterface, Serializable {

    @Override
    public void observeInProgress(@Observes(during = IN_PROGRESS) Bark event) {
        Actions.add(IN_PROGRESS);
    }

    @Override
    public void observeAfterCompletion(@Observes(during = AFTER_COMPLETION) Bark someEvent) {
        Actions.add(AFTER_COMPLETION);
    }

    @Override
    public void observeAfterSuccess(@Observes(during = AFTER_SUCCESS) Bark event) {
        Actions.add(AFTER_SUCCESS);
    }

    @Override
    public void observeAfterSuccessWithHighPriority(@Priority(1) @Observes(during = AFTER_SUCCESS) Bark event) {
        Actions.add(AFTER_SUCCESS + "1");
    }

    @Override
    public void observeAfterSuccessWithLowPriority(@Priority(100) @Observes(during = AFTER_SUCCESS) Bark event) {
        Actions.add(AFTER_SUCCESS + "100");
    }

    @Override
    public void observeAfterFailure(@Observes(during = AFTER_FAILURE) Bark event) {
        Actions.add(AFTER_FAILURE);
    }

    @Override
    public void observeBeforeCompletion(@Observes(during = BEFORE_COMPLETION) Bark event) {
        Actions.add(BEFORE_COMPLETION);
    }

    @Override
    public void observeAndFail(@Observes(during = BEFORE_COMPLETION) @Gnarly Bark event) throws FooException {
        Actions.add(BEFORE_COMPLETION);
        throw new FooException();
    }
}
