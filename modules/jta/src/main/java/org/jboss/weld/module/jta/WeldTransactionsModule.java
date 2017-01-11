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
package org.jboss.weld.module.jta;

import org.jboss.weld.module.ObserverNotifierFactory;
import org.jboss.weld.module.WeldModule;
import org.jboss.weld.transaction.spi.TransactionServices;

/**
 * Module that provides JTA integration.
 *
 * @author Jozef Hartinger
 *
 */
public class WeldTransactionsModule implements WeldModule {

    @Override
    public String getName() {
        return "weld-jta";
    }

    @Override
    public void postServiceRegistration(PostServiceRegistrationContext ctx) {
        if (ctx.getServices().contains(TransactionServices.class)) {
            ctx.getServices().add(ObserverNotifierFactory.class, TransactionalObserverNotifier.FACTORY);
        }
    }

    @Override
    public void preBeanRegistration(PreBeanRegistrationContext ctx) {
        if (ctx.getBeanManager().getServices().contains(TransactionServices.class)) {
            ctx.registerBean(new UserTransactionBean(ctx.getBeanManager()));
        }
    }

}
