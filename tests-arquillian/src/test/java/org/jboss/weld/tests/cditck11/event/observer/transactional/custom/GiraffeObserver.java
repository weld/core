/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.cditck11.event.observer.transactional.custom;

import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;

public class GiraffeObserver {

    /**
     * 
     * @param withdrawal
     * @throws Exception
     */
    public void withdrawAfterCompletion(@Observes(during = TransactionPhase.AFTER_COMPLETION) Giraffe giraffe) throws Exception {
        logEventFired(TransactionPhase.AFTER_COMPLETION);
    }

    /**
     * 
     * @param withdrawal
     * @throws Exception
     */
    public void withdrawBeforeCompletion(@Observes(during = TransactionPhase.BEFORE_COMPLETION) Giraffe giraffe)
            throws Exception {
        logEventFired(TransactionPhase.BEFORE_COMPLETION);
    }

    private void logEventFired(TransactionPhase phase) {
        ActionSequence.addAction(phase.toString());
    }
}
