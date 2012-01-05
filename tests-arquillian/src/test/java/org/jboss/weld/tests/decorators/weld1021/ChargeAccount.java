/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.weld.tests.decorators.weld1021;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Decorator
public abstract class ChargeAccount implements Account {

    private static final int WITHDRAVAL_CHARGE = 5;

    public static int charged = 0;

    @Inject
    @Delegate
    private Account delegate;

    public void withdraw(int amount) {
        delegate.withdraw(amount + WITHDRAVAL_CHARGE);
        charged += WITHDRAVAL_CHARGE;
    }

    public abstract void deposit(int amount);

    public static void reset() {
        charged = 0;
    }
}
