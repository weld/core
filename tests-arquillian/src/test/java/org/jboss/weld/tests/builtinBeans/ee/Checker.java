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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

public class Checker {

    public static boolean checkUserTransaction(UserTransaction userTransaction) {
        try {
            if (userTransaction != null) {
                userTransaction.getStatus();
                return true;
            }
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static boolean checkEntityManager(EntityManager entityManager) {
        if (entityManager != null) {
            return !entityManager.contains(new Foo());
        } else {
            return false;
        }
    }

    public static boolean checkEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        if (entityManagerFactory != null) {
            return entityManagerFactory.isOpen();
        } else {
            return false;
        }
    }

    public static boolean checkRemoteEjb(HorseRemote horse) {
        if (horse != null) {
            return horse.ping();
        } else {
            return false;
        }
    }

}
