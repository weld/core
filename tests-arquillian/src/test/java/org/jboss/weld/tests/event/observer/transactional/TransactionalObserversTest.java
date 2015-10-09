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

import static javax.enterprise.event.TransactionPhase.AFTER_COMPLETION;
import static javax.enterprise.event.TransactionPhase.AFTER_FAILURE;
import static javax.enterprise.event.TransactionPhase.AFTER_SUCCESS;
import static javax.enterprise.event.TransactionPhase.BEFORE_COMPLETION;
import static javax.enterprise.event.TransactionPhase.IN_PROGRESS;
import static org.jboss.weld.tests.event.observer.transactional.DogAgent.EVENT_FIRED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Integration tests for Web Bean events.
 *
 * @author David Allen
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class TransactionalObserversTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(TransactionalObserversTest.class))
                .addPackage(TransactionalObserversTest.class.getPackage())
                .addClass(Utils.class);
    }

    @Inject
    @Tame
    private PomeranianInterface dog;

    @Inject
    private Agent dogAgent;

    @Before
    public void reset() {
        assertNotNull(dogAgent);
        Actions.clear();
    }

    @Test
    public void testNoTransaction() {
        dogAgent.sendOutsideTransaction(new Bark());
        assertTrue(Actions.contains(BEFORE_COMPLETION, AFTER_SUCCESS, AFTER_FAILURE, AFTER_COMPLETION, IN_PROGRESS));
        assertTrue(Actions.endsWith(EVENT_FIRED));
    }

    @Test
    public void testSuccess() {
        dogAgent.sendInTransaction(new Bark());
        assertTrue(Actions.startsWith(IN_PROGRESS, EVENT_FIRED, BEFORE_COMPLETION));
        assertTrue(Actions.precedes(BEFORE_COMPLETION, AFTER_SUCCESS, AFTER_COMPLETION));
        assertTrue(Actions.precedes(AFTER_SUCCESS + "100", AFTER_SUCCESS, AFTER_COMPLETION));
        assertTrue(Actions.precedes(AFTER_SUCCESS + "1", AFTER_SUCCESS + "100"));
        assertFalse(Actions.contains(AFTER_FAILURE));
    }

    @Test
    public void testTransactionFailure() throws Exception {
        dogAgent.sendInTransactionAndFail(new Bark());
        assertTrue(Actions.startsWith(IN_PROGRESS, EVENT_FIRED));
        assertTrue(Actions.precedes(EVENT_FIRED, AFTER_FAILURE, AFTER_COMPLETION));
        assertFalse(Actions.contains(BEFORE_COMPLETION));
        assertFalse(Actions.contains(AFTER_SUCCESS));
    }

    @Test
    public void testSuccessAfterTransactionalObserverException() {
        dogAgent.sendInTransaction(new Bark(), new GnarlyLiteral());
        assertTrue(Actions.startsWith(IN_PROGRESS, EVENT_FIRED, BEFORE_COMPLETION));
        assertTrue(Actions.precedes(BEFORE_COMPLETION, AFTER_SUCCESS, AFTER_COMPLETION));
        assertFalse(Actions.contains(AFTER_FAILURE));
    }
}
