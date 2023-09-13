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
package org.jboss.weld.tests.builtinBeans.ee;

import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkEntityManager;
import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkEntityManagerFactory;
import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkRemoteEjb;
import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkUserTransaction;

import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.UserTransaction;

import org.junit.Assert;

@SessionScoped
public class EEResourceConsumer implements Serializable {

    @Inject
    @Produced
    UserTransaction userTransaction;
    @Inject
    @Produced
    EntityManager entityManager;
    @Inject
    @Produced
    EntityManagerFactory entityManagerFactory;
    @Inject
    @Produced
    HorseRemote horse;

    public void check() {
        Assert.assertTrue(checkUserTransaction(userTransaction));
        Assert.assertTrue(checkEntityManager(entityManager));
        Assert.assertTrue(checkEntityManagerFactory(entityManagerFactory));
        Assert.assertTrue(checkRemoteEjb(horse));
    }

}
