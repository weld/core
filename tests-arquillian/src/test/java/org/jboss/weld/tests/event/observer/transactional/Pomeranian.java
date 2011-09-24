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

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Named;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.math.BigInteger;
import java.util.logging.Logger;

import static javax.ejb.TransactionManagementType.BEAN;
import static javax.enterprise.event.TransactionPhase.AFTER_COMPLETION;
import static javax.enterprise.event.TransactionPhase.AFTER_FAILURE;
import static javax.enterprise.event.TransactionPhase.AFTER_SUCCESS;
import static javax.enterprise.event.TransactionPhase.BEFORE_COMPLETION;
import static javax.transaction.Status.STATUS_NO_TRANSACTION;


@Stateful
@TransactionManagement(BEAN)
@Tame
@Named("Teddy")
@SessionScoped
public class Pomeranian implements PomeranianInterface {
    private static final Logger log = Logger.getLogger(Pomeranian.class.getName());

    @Resource
    private UserTransaction transaction;

    @Resource
    private SessionContext context;

    private boolean correctContext = false;
    private boolean correctTransactionState = false;

    /**
     * Observes a String event only after the transaction is completed.
     *
     * @param someEvent
     */
    public void observeStringEvent(@Observes(during = AFTER_COMPLETION) String someEvent) {
        try {
            log.finer("Observing string event with tx status: " + transaction.getStatus());
            if (transaction.getStatus() == STATUS_NO_TRANSACTION) {
                setCorrectTransactionState(true);
            }
        } catch (SystemException e) {
            throw new EJBException("Failed to detect transaction status", e);
        }
    }

    /**
     * Observes an Integer event if the transaction is successfully completed.
     *
     * @param event
     */
    public void observeIntegerEvent(@Observes(during = AFTER_SUCCESS) Integer event) {
        try {
            log.finer("Observing integer event with tx status: " + transaction.getStatus());
            if (transaction.getStatus() == STATUS_NO_TRANSACTION) {
                setCorrectTransactionState(true);
            }
        } catch (SystemException e) {
            throw new EJBException("Failed to detect transaction status", e);
        }
    }

    /**
     * Observes a Float event only if the transaction failed.
     *
     * @param event
     */
    public void observeFloatEvent(@Observes(during = AFTER_FAILURE) Float event) {
        try {
            log.finer("Observing float event with tx status: " + transaction.getStatus());
            if (transaction.getStatus() == STATUS_NO_TRANSACTION) {
                setCorrectTransactionState(true);
            }
        } catch (SystemException e) {
            throw new EJBException("Failed to detect transaction status", e);
        }
    }

    public void observeBigIntegerEvent(@Observes BigInteger event) {
        try {
            log.finer("Observing BigInteger event with tx status: " + transaction.getStatus());
            if (transaction.getStatus() == STATUS_NO_TRANSACTION) {
                setCorrectTransactionState(true);
            }

//         if (context.getCallerPrincipal().getName().equals("Bubba"))
//         {
//            setCorrectContext(true);
//         }
//         log.finer("Principal caller is " + context.getCallerPrincipal().getName());
        } catch (SystemException e) {
            throw new EJBException("Failed to detect transaction status", e);
        }
    }

    public void observeExceptionEvent(@Observes(during = BEFORE_COMPLETION) RuntimeException event) {
        try {
            log.finer("Observing exception event with tx status: " + transaction.getStatus());
            if (transaction.getStatus() == STATUS_NO_TRANSACTION) {
                setCorrectTransactionState(true);
            }

//         if (context.getCallerPrincipal().getName().equals("Bubba"))
//         {
//            setCorrectContext(true);
//         }
//         log.finer("Principal caller is " + context.getCallerPrincipal().getName());
        } catch (SystemException e) {
            throw new EJBException("Failed to detect transaction status", e);
        }
    }

    public void observeCharEvent(Character event) {
        try {
            log.finer("Observing character event with tx status: " + transaction.getStatus());
            if (transaction.getStatus() == STATUS_NO_TRANSACTION) {
                setCorrectTransactionState(true);
            }
        } catch (SystemException e) {
            throw new EJBException("Failed to detect transaction status", e);
        }
    }

    public boolean isCorrectContext() {
        return correctContext;
    }

    public void setCorrectContext(boolean correctContext) {
        this.correctContext = correctContext;
    }

    public boolean isCorrectTransactionState() {
        return correctTransactionState;
    }

    public void setCorrectTransactionState(boolean correctTransactionState) {
        this.correctTransactionState = correctTransactionState;
    }

    @Remove
    public void removeSessionBean() {

    }

}
