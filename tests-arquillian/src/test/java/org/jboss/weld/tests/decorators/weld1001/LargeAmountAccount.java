/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.decorators.weld1001;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Decorator
public abstract class LargeAmountAccount extends AbstractAccount {

    @Inject
    @Large
    @Delegate
    private Account<Double> delegate;

    public void withdraw(Double amount) {
        System.out.println("Before [" + getState() + "], withdrawing amount = " + amount);
        delegate.withdraw(amount);
        System.out.println("After [" + getState() + "], withdrawn amount = " + amount);
    }

    public void deposit(Double amount) {
        System.out.println("Before [" + getState() + "], depositing amount = " + amount);
        delegate.deposit(amount);
        System.out.println("After [" + getState() + "], deposited amount = " + amount);
    }

}
