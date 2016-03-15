/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.event.observer.transactional.rollback;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;

import org.jboss.weld.test.util.ActionSequence;

@ApplicationScoped
public class FooTransactionalObserver {

    public void observesProgress(@Observes(during = TransactionPhase.IN_PROGRESS) Foo foo) {
        ActionSequence.addAction(TransactionPhase.IN_PROGRESS.toString());
    }

    public void observesBeforeCompletion(@Observes(during = TransactionPhase.BEFORE_COMPLETION) Foo foo) {
        ActionSequence.addAction(TransactionPhase.BEFORE_COMPLETION.toString());
    }

    public void observesAfterCompletion(@Observes(during = TransactionPhase.AFTER_COMPLETION) Foo foo) {
        ActionSequence.addAction(TransactionPhase.AFTER_COMPLETION.toString());
    }

    public void observesAfterFailure(@Observes(during = TransactionPhase.AFTER_FAILURE) Foo foo) {
        ActionSequence.addAction(TransactionPhase.AFTER_FAILURE.toString());
    }

    public void observesAfterSuccess(@Observes(during = TransactionPhase.AFTER_SUCCESS) Foo foo) {
        ActionSequence.addAction(TransactionPhase.AFTER_SUCCESS.toString());
    }

}
